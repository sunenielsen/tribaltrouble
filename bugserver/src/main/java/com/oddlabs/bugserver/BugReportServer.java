package com.oddlabs.bugserver;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oddlabs.bugreport.BugReport;
import com.oddlabs.bugreport.BugReportProtocol;
import com.oddlabs.util.BuildInfo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Accepts crash reports from game clients, stores each one under
 * {@code <reports_dir>/<yyyy-MM-dd>/<id>/} and files it as a work item in Plane. Plain HTTP only: TLS for
 * bug.tribaltrouble.org is terminated by the reverse proxy in front of it (see deploy/).
 *
 * <p>The client gets its answer as soon as the report is on disk; the Plane work item is created in the background.
 * Reports that could not be filed (Plane down, misconfigured) are retried periodically, so nothing is lost.
 *
 * <p>Configured through system properties ({@code -Dbugserver.<key>}) or environment variables
 * ({@code BUGSERVER_<KEY>}), see deploy/README.md.
 */
public final class BugReportServer {
    private static final Logger logger = Logger.getLogger(BugReportServer.class.getName());

    private static final String EVENT_LOG_FILE = "event.log.gz";
    private static final String STACK_TRACE_FILE = "stack_trace.txt";
    private static final String META_FILE = "report.properties";
    private static final String META_PLANE_ISSUE = "plane_issue";

    private static final byte[] GZIP_MAGIC = {(byte) 0x1f, (byte) 0x8b};
    private static final int MAX_FIELD_LENGTH = 200;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final long RATE_WINDOW_MILLIS = 60 * 60 * 1000L;
    private static final long RETRY_MINUTES = 15;

    private final ObjectMapper mapper = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Path reports_dir;
    private final int per_ip_hourly;
    private final long min_free_bytes;
    private final PlaneClient plane;
    /** Single thread, so a report is never filed twice concurrently. */
    private final ScheduledExecutorService plane_worker = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Deque<Long>> recent_reports = new HashMap<>();

    private BugReportServer(Path reports_dir, int per_ip_hourly, long min_free_bytes, PlaneClient plane) {
        this.reports_dir = reports_dir;
        this.per_ip_hourly = per_ip_hourly;
        this.min_free_bytes = min_free_bytes;
        this.plane = plane;
    }

    private static String config(String key, String default_value) {
        String value = System.getProperty("bugserver." + key);
        if (value == null)
            value = System.getenv("BUGSERVER_" + key.toUpperCase());
        return value != null && !value.isBlank() ? value.trim() : default_value;
    }

    private static PlaneClient planeFromConfig() {
        String url = config("plane_url", "");
        String api_key = config("plane_api_key", "");
        String workspace = config("plane_workspace", "");
        String project = config("plane_project", "");
        if (url.isEmpty() || api_key.isEmpty() || workspace.isEmpty() || project.isEmpty()) {
            logger.warning(
                    "Plane is not configured (need plane_url, plane_api_key, plane_workspace, plane_project); " + "reports will only be stored on disk");
            return null;
        }
        List<String> labels = Arrays.stream(config("plane_labels", "").split(",")).map(String::trim).filter(
                s -> !s.isEmpty()).toList();
        return new PlaneClient(url, api_key, workspace, project, config("plane_items_path", "work-items"), config(
                "plane_priority", "high"), labels);
    }

    public static void main(String[] args) throws IOException {
        String bind = config("bind", "127.0.0.1");
        int port = Integer.parseInt(config("port", "8091"));
        Path dir = Path.of(config("dir", "/var/bugreports"));
        int per_ip_hourly = Integer.parseInt(config("per_ip_hourly", "20"));
        long min_free_bytes = Long.parseLong(config("min_free_mb", "1024")) * 1024 * 1024;

        Files.createDirectories(dir);
        BugReportServer handler = new BugReportServer(dir, per_ip_hourly, min_free_bytes, planeFromConfig());

        HttpServer server = HttpServer.create(new InetSocketAddress(bind, port), 0);
        server.createContext(BugReportProtocol.REPORT_PATH, handler::handleReport);
        server.createContext(BugReportProtocol.HEALTH_PATH, exchange -> respond(exchange, 200, "ok"));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        if (handler.plane != null) {
            var _ = handler.plane_worker.scheduleWithFixedDelay(handler::fileUnfiledReports, 0, RETRY_MINUTES,
                    TimeUnit.MINUTES);
        }
        logger.info(
                "Bug report server " + BuildInfo.DESCRIPTION + " listening on " + bind + ":" + port + ", storing reports in " + dir.toAbsolutePath());
    }

    private void handleReport(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.getResponseHeaders().set("Allow", "POST");
                respond(exchange, 405, "POST only");
                return;
            }
            String client = clientAddress(exchange);
            if (!allow(client)) {
                respond(exchange, 429, "too many reports");
                return;
            }
            if (Files.getFileStore(reports_dir).getUsableSpace() < min_free_bytes) {
                logger.warning("Rejecting report from " + client + ": storage is low on space");
                respond(exchange, 507, "storage full");
                return;
            }
            String length_header = exchange.getRequestHeaders().getFirst("Content-Length");
            if (length_header != null && parseLong(length_header) > BugReportProtocol.MAX_REQUEST_BYTES) {
                respond(exchange, 413, "report too large");
                return;
            }
            byte[] body;
            try (InputStream in = exchange.getRequestBody()) {
                body = in.readNBytes(BugReportProtocol.MAX_REQUEST_BYTES + 1);
            }
            if (body.length > BugReportProtocol.MAX_REQUEST_BYTES) {
                respond(exchange, 413, "report too large");
                return;
            }
            BugReport report;
            try {
                report = mapper.readValue(body, BugReport.class);
            } catch (JacksonException e) {
                respond(exchange, 400, "malformed report: " + e.getOriginalMessage());
                return;
            }
            String problem = validate(report);
            if (problem != null) {
                respond(exchange, 400, problem);
                return;
            }
            Path report_dir = store(client, report);
            logger.info("Stored report " + report_dir.getFileName() + " from " + client);
            if (plane != null)
                plane_worker.execute(() -> fileReport(report_dir));
            respond(exchange, 201, report_dir.getFileName().toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed handling report", e);
            try {
                respond(exchange, 500, "internal error");
            } catch (IOException _) {
                // response already started or connection gone
            }
        }
    }

    private static String validate(BugReport report) {
        if (report.stack_trace() == null || report.stack_trace().isBlank())
            return "stack_trace is required";
        if (report.stack_trace().length() > BugReportProtocol.MAX_STACK_TRACE_CHARS)
            return "stack_trace too long";
        byte[] event_log = report.event_log();
        if (event_log != null) {
            if (event_log.length > BugReportProtocol.MAX_EVENT_LOG_BYTES)
                return "event_log too large";
            if (event_log.length < GZIP_MAGIC.length || event_log[0] != GZIP_MAGIC[0] || event_log[1] != GZIP_MAGIC[1])
                return "event_log must be gzipped";
        }
        return null;
    }

    private Path store(String client, BugReport report) throws IOException {
        Instant now = Instant.now();
        String id = now.toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
        Path day_dir = reports_dir.resolve(LocalDate.ofInstant(now, ZoneOffset.UTC).toString());
        Files.createDirectories(day_dir);

        // Assemble in a hidden temp dir and rename, so the retry sweep only ever sees complete reports.
        Path tmp = Files.createTempDirectory(day_dir, ".incoming-");
        Files.writeString(tmp.resolve(STACK_TRACE_FILE), report.stack_trace());
        if (report.event_log() != null)
            Files.write(tmp.resolve(EVENT_LOG_FILE), report.event_log());
        Properties meta = new Properties();
        meta.setProperty("id", id);
        meta.setProperty("received", now.toString());
        meta.setProperty("client", client);
        meta.setProperty("version", sanitize(report.version()));
        meta.setProperty("commit", sanitize(report.commit()));
        meta.setProperty("dirty", Boolean.toString(report.dirty()));
        meta.setProperty("os", sanitize(report.os()));
        meta.setProperty("java", sanitize(report.java()));
        meta.setProperty("event_log_size", report.event_log() != null ? Integer.toString(
                report.event_log().length) : "none");
        writeMeta(tmp, meta);
        Path report_dir = day_dir.resolve(id);
        Files.move(tmp, report_dir, StandardCopyOption.ATOMIC_MOVE);
        return report_dir;
    }

    // --- Plane -----------------------------------------------------------------------------------

    private void fileUnfiledReports() {
        try (Stream<Path> days = Files.list(reports_dir)) {
            for (Path day : days.filter(Files::isDirectory).sorted().toList()) {
                try (Stream<Path> reports = Files.list(day)) {
                    reports.filter(p -> !p.getFileName().toString().startsWith(".")).sorted().forEach(
                            this::fileReport);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed sweeping for unfiled reports", e);
        }
    }

    private void fileReport(Path report_dir) {
        try {
            Properties meta = readMeta(report_dir);
            if (meta.getProperty(META_PLANE_ISSUE) != null)
                return;
            String stack_trace = Files.readString(report_dir.resolve(STACK_TRACE_FILE));
            Path event_log = report_dir.resolve(EVENT_LOG_FILE);
            boolean has_event_log = Files.exists(event_log);

            PlaneClient.WorkItem item = plane.createWorkItem(title(stack_trace), description(meta, stack_trace,
                    has_event_log));
            // Record the work item before attaching, so a failed attachment never causes a duplicate work item.
            meta.setProperty(META_PLANE_ISSUE, item.id());
            meta.setProperty("plane_sequence_id", item.sequence_id());
            writeMeta(report_dir, meta);
            logger.info("Filed report " + meta.getProperty("id") + " as Plane work item " + item.sequence_id());

            if (has_event_log) {
                try {
                    plane.attach(item.id(), EVENT_LOG_FILE, "application/gzip", Files.readAllBytes(event_log));
                } catch (IOException e) {
                    logger.log(Level.WARNING,
                            "Could not attach event log to Plane work item " + item.sequence_id() + "; it is still on disk in " + report_dir,
                            e);
                    meta.setProperty("plane_attachment_error", sanitize(e.getMessage()));
                    writeMeta(report_dir, meta);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "Could not file report " + report_dir.getFileName() + " in Plane, will retry in " + RETRY_MINUTES + " minutes",
                    e);
        }
    }

    private static String title(String stack_trace) {
        String first_line = stack_trace.strip().lines().findFirst().orElse("Unknown crash");
        String title = "Crash: " + first_line;
        return title.length() > MAX_TITLE_LENGTH ? title.substring(0, MAX_TITLE_LENGTH - 3) + "..." : title;
    }

    private static String description(Properties meta, String stack_trace, boolean has_event_log) {
        String commit = meta.getProperty("commit", "");
        StringBuilder html = new StringBuilder();
        html.append("<h3>Build</h3><ul>");
        item(html, "Version", meta.getProperty("version"));
        item(html, "Commit", commit + (Boolean.parseBoolean(meta.getProperty("dirty")) ? " (dirty)" : ""));
        item(html, "OS", meta.getProperty("os"));
        item(html, "Java", meta.getProperty("java"));
        item(html, "Report", meta.getProperty("id") + ", received " + meta.getProperty("received"));
        html.append("</ul>");
        html.append("<h3>Stack trace</h3><pre><code>").append(escape(stack_trace)).append("</code></pre>");
        html.append("<h3>Reproduce</h3>");
        if (has_event_log) {
            html.append("<p>Check out <code>").append(escape(commit)).append(
                    "</code>, download the attached <code>").append(EVENT_LOG_FILE).append("</code> and run:</p>");
            html.append("<pre><code>./gradlew :tt:run --args=\"--eventload zipped /path/to/").append(
                    EVENT_LOG_FILE).append("\"</code></pre>");
            html.append("<p>Also stored on the bug server at <code>").append(escape(meta.getProperty("id"))).append(
                    "/").append(EVENT_LOG_FILE).append("</code>.</p>");
        } else {
            html.append("<p>No event log was recorded for this run.</p>");
        }
        return html.toString();
    }

    private static void item(StringBuilder html, String label, String value) {
        html.append("<li><strong>").append(label).append(":</strong> ").append(escape(value)).append("</li>");
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static Properties readMeta(Path report_dir) throws IOException {
        Properties meta = new Properties();
        try (Reader in = Files.newBufferedReader(report_dir.resolve(META_FILE), StandardCharsets.UTF_8)) {
            meta.load(in);
        }
        return meta;
    }

    private static void writeMeta(Path report_dir, Properties meta) throws IOException {
        Path tmp = report_dir.resolve(META_FILE + ".tmp");
        try (Writer out = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            meta.store(out, "Tribal Trouble bug report");
        }
        Files.move(tmp, report_dir.resolve(META_FILE), StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

    // --- HTTP helpers ----------------------------------------------------------------------------

    /** Sliding one-hour window per client address. */
    private synchronized boolean allow(String client) {
        long now = System.currentTimeMillis();
        recent_reports.values().removeIf(times -> {
            while (!times.isEmpty() && now - times.peekFirst() > RATE_WINDOW_MILLIS)
                times.pollFirst();
            return times.isEmpty();
        });
        Deque<Long> times = recent_reports.computeIfAbsent(client, _ -> new ArrayDeque<>());
        if (times.size() >= per_ip_hourly)
            return false;
        times.addLast(now);
        return true;
    }

    /** Trusts X-Real-IP / X-Forwarded-For only when the request came through a local proxy. */
    private static String clientAddress(HttpExchange exchange) {
        InetAddress remote = exchange.getRemoteAddress().getAddress();
        if (remote.isLoopbackAddress()) {
            String real_ip = exchange.getRequestHeaders().getFirst("X-Real-IP");
            if (real_ip != null && !real_ip.isBlank())
                return sanitize(real_ip);
            String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String[] hops = forwarded.split(",");
                return sanitize(hops[hops.length - 1]);
            }
        }
        return remote.getHostAddress();
    }

    private static String sanitize(String value) {
        if (value == null)
            return "";
        String clean = value.replaceAll("\\p{Cntrl}", " ").trim();
        return clean.length() > MAX_FIELD_LENGTH ? clean.substring(0, MAX_FIELD_LENGTH) : clean;
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException _) {
            return -1;
        }
    }

    private static void respond(HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = (message + "\n").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
        exchange.close();
    }
}
