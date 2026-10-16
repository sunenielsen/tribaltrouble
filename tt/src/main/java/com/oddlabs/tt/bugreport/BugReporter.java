package com.oddlabs.tt.bugreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oddlabs.bugreport.BugReport;
import com.oddlabs.bugreport.BugReportProtocol;
import com.oddlabs.tt.global.Settings;
import com.oddlabs.util.BuildInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * Uploads crash reports (stack trace, build info and event log) to the bug report server
 * (bug.tribaltrouble.org), which files them in Plane so the crashed run can be replayed on our side. Override the
 * endpoint with
 * {@code -Dcom.oddlabs.tt.bugreport.url=http://127.0.0.1:8091/report} for local testing.
 */
public final class BugReporter {
    private static final Logger logger = Logger.getLogger(BugReporter.class.getName());

    private static final String DEFAULT_URL = "https://bug." + Settings.OFFICIAL_DOMAIN + BugReportProtocol.REPORT_PATH;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private BugReporter() {
    }

    public static @NonNull URI reportUri() {
        return URI.create(System.getProperty("com.oddlabs.tt.bugreport.url", DEFAULT_URL));
    }

    /**
     * Uploads a crash report, blocking until the server answers.
     *
     * @param stack_trace the crash's stack trace
     * @param event_log   the run's event log, or null if it was not being recorded
     * @return the report id assigned by the server
     */
    public static @NonNull String submit(@NonNull String stack_trace,
            @Nullable Path event_log) throws IOException, InterruptedException {
        byte[] event_log_gz = event_log != null ? gzip(event_log) : null;
        if (event_log_gz != null && event_log_gz.length > BugReportProtocol.MAX_EVENT_LOG_BYTES)
            throw new IOException("Event log too large to report: " + event_log_gz.length + " bytes compressed");
        if (stack_trace.length() > BugReportProtocol.MAX_STACK_TRACE_CHARS)
            stack_trace = stack_trace.substring(0, BugReportProtocol.MAX_STACK_TRACE_CHARS);

        String os = System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty(
                "os.arch");
        BugReport report = new BugReport(BuildInfo.FULL_VERSION, BuildInfo.GIT_COMMIT, BuildInfo.GIT_DIRTY, os,
                Runtime.version().toString(), stack_trace, event_log_gz);
        byte[] body = new ObjectMapper().writeValueAsBytes(report);

        URI uri = reportUri();
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
        builder.timeout(REQUEST_TIMEOUT);
        builder.header("Content-Type", BugReportProtocol.CONTENT_TYPE);
        builder.header("User-Agent", "TribalTrouble/" + BuildInfo.VERSION);
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
        HttpClient.Builder client_builder = HttpClient.newBuilder();
        client_builder.connectTimeout(CONNECT_TIMEOUT);
        client_builder.followRedirects(HttpClient.Redirect.NEVER);
        try (HttpClient client = client_builder.build()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String message = response.body().trim();
            if (response.statusCode() / 100 != 2)
                throw new IOException("Bug report rejected by " + uri + ": " + response.statusCode() + " " + message);
            logger.info("Bug report " + message + " sent to " + uri + " (" + body.length + " bytes)");
            return message;
        }
    }

    /** Convenience for crash handlers: reports {@code t} with its full stack trace. */
    public static @NonNull String submit(@NonNull Throwable t,
            @Nullable Path event_log) throws IOException, InterruptedException {
        StringWriter trace = new StringWriter();
        t.printStackTrace(new PrintWriter(trace));
        return submit(trace.toString(), event_log);
    }

    private static byte @NonNull [] gzip(@NonNull Path file) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream in = Files.newInputStream(file); OutputStream out = new GZIPOutputStream(bytes)) {
            in.transferTo(out);
        }
        return bytes.toByteArray();
    }
}
