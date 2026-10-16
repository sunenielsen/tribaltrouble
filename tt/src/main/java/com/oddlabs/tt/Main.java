package com.oddlabs.tt;

import com.oddlabs.tt.bugreport.BugReporter;
import com.oddlabs.tt.event.LocalEventQueue;
import com.oddlabs.tt.render.Renderer;
import com.oddlabs.tt.steam.SteamManager;
import com.oddlabs.tt.util.Utils;
import org.jspecify.annotations.NonNull;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final ResourceBundle bundle = ResourceBundle.getBundle(Main.class.getName());

    private static @NonNull String i18n(@NonNull String key, @NonNull Object @NonNull... args) {
        return Utils.getBundleString(bundle, key, args);
    }

    /** Reports a failure the game keeps running after. */
    public static void fail(@NonNull Throwable t) {
        fail(t, false);
    }

    /**
     * Reports a failure the game exits after, offering to send a bug report. The event log is ended in the process,
     * so the game must not keep running afterwards.
     */
    public static void failFatal(@NonNull Throwable t) {
        fail(t, true);
    }

    private static void fail(@NonNull Throwable t, boolean fatal) {
        logger.log(Level.SEVERE, "Critical Failure", t);

        if (!Boolean.getBoolean("com.oddlabs.tt.developer")) {
            Throwable root = t;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String error = i18n("error");
            String error_msg;
            try {
                error_msg = i18n("error_message", root.toString());
            } catch (IllegalArgumentException e) {
                // Fallback if message formatting fails (e.g. quotes in exception message)
                error_msg = "Error: " + root;
            }
            logger.log(Level.SEVERE, error + ": " + error_msg);
            // Replays are reproductions of an already reported run; don't report them again.
            if (fatal && !LocalEventQueue.getQueue().getDeterministic().isPlayback()) {
                if (messageBox(error, error_msg + "\n\n" + i18n("send_report"), "yesno", "error") == 1)
                    sendReport(t);
            } else {
                messageBox(error, error_msg, "ok", "error");
            }
        }
    }

    private static void sendReport(@NonNull Throwable t) {
        LocalEventQueue queue = LocalEventQueue.getQueue();
        Path event_log = queue.getEventLogFile();
        try {
            // Usually already ended by Renderer.cleanup(), but not when the crash happened during startup.
            queue.close();
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Could not end the event log", e);
        }
        if (event_log != null && !Files.isRegularFile(event_log))
            event_log = null;
        try {
            String id = BugReporter.submit(t, event_log);
            messageBox(i18n("report_sent_title"), i18n("report_sent", id), "ok", "info");
        } catch (IOException | RuntimeException e) {
            logger.log(Level.WARNING, "Could not send bug report", e);
            messageBox(i18n("error"), i18n("report_failed"), "ok", "warning");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** @return 1 for ok/yes, 0 for cancel/no */
    private static int messageBox(@NonNull String title, @NonNull String message, @NonNull String dialog_type,
            @NonNull String icon_type) {
        return TinyFileDialogs.tinyfd_messageBox(withoutQuotes(title), withoutQuotes(message), dialog_type, icon_type,
                1);
    }

    /**
     * tinyfd refuses to show text containing ASCII quotes (it shows "INVALID MESSAGE WITH QUOTES" instead), so swap
     * them for typographic ones.
     */
    private static @NonNull String withoutQuotes(@NonNull String text) {
        StringBuilder result = new StringBuilder(text.length());
        boolean open = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                result.append(open ? '“' : '”');
                open = !open;
            } else if (c == '\'') {
                result.append('’');
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static void shutdown(int status) {
        SteamManager.shutdown();
        Renderer.getRenderer().close();
        logger.info("Exiting");
        System.exit(status);
    }

    static void main(@NonNull String @NonNull... args) {
        int status = 1;
        try {
            SteamManager.init();
            logger.info("Starting game....");
            Renderer.getRenderer().run(args);
            status = 0;
        } catch (Throwable t) {
            failFatal(t);
        } finally {
            shutdown(status);
        }
    }
}
