package com.oddlabs.bugreport;

import org.jspecify.annotations.Nullable;

/**
 * A crash report as sent by the game to the bug report server.
 *
 * @param version     {@code BuildInfo.FULL_VERSION} of the crashed game
 * @param commit      {@code BuildInfo.GIT_COMMIT} the game was built from
 * @param dirty       {@code BuildInfo.GIT_DIRTY}, whether the build had uncommitted changes
 * @param os          operating system name, version and architecture
 * @param java        Java runtime version
 * @param stack_trace the full stack trace of the crash
 * @param event_log   the gzipped {@code event.log} of the crashed run (base64 on the wire), or null if the run was
 *                    not being recorded
 */
public record BugReport(@Nullable String version, @Nullable String commit, boolean dirty, @Nullable String os,
                        @Nullable String java, @Nullable String stack_trace, byte @Nullable [] event_log) {
}
