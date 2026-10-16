package com.oddlabs.bugreport;

/**
 * Wire format shared by the game client and the bug report server.
 *
 * <p>A report is a single {@code POST} to {@link #REPORT_PATH} whose body is a {@link BugReport} serialized as
 * JSON. The event log inside it is gzipped, and the server stores it verbatim as {@code event.log.gz} so it can be
 * replayed with {@code --eventload zipped <path>}.
 */
public final class BugReportProtocol {
    public static final String REPORT_PATH = "/report";
    public static final String HEALTH_PATH = "/health";

    public static final String CONTENT_TYPE = "application/json";

    /** Upper bound on the gzipped event log. */
    public static final int MAX_EVENT_LOG_BYTES = 32 * 1024 * 1024;
    /** Upper bound on the stack trace, in characters. */
    public static final int MAX_STACK_TRACE_CHARS = 256 * 1024;
    /** Upper bound on the whole request body; leaves room for base64 expansion of the event log. */
    public static final int MAX_REQUEST_BYTES = 48 * 1024 * 1024;

    private BugReportProtocol() {
    }
}
