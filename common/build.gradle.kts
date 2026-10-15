plugins {
    `java-library`
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")
    api("org.joml:joml:1.10.8")
    api("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// --- BuildInfo generation ---
// Writes BuildInfo.java with VERSION = MAJOR.MINOR.PATCH and FULL_VERSION = "v<VERSION>-<API_VERSION>".
// PATCH = commits since the bump commit that introduced the current MAJOR.MINOR in build.gradle.kts.
// Must stay in sync with the `version` job in .github/workflows/gradle.yml.

val generatedBuildInfoDir = layout.buildDirectory.dir("generated/sources/buildinfo/java/main")

val generateBuildInfo by tasks.registering {
    val outputDir = generatedBuildInfoDir
    val baseVersion = project.version.toString()
    val gitDir = project.rootDir
    outputs.dir(outputDir)
    outputs.upToDateWhen { false }  // git history changes between commits; always recompute

    doLast {
        fun git(vararg args: String): String {
            val process = ProcessBuilder("git", *args)
                .directory(gitDir)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            check(process.waitFor() == 0) { "git ${args.joinToString(" ")} failed: $output" }
            return output
        }

        // Regex avoids passing literal " chars through ProcessBuilder (Windows quoting issue).
        // `.` matches the surrounding quote chars in: version = "2.0"
        val versionPattern = "version = ." + baseVersion.replace(".", "\\.") + "."
        val patch = runCatching {
            val anchor = git("log", "--format=%H", "-G", versionPattern, "--", "build.gradle.kts")
                .lineSequence()
                .firstOrNull()
                .orEmpty()
            if (anchor.isEmpty()) "0" else git("rev-list", "--count", "$anchor..HEAD").ifEmpty { "0" }
        }.getOrDefault("0")

        val commit = runCatching { git("rev-parse", "--short=12", "HEAD") }.getOrDefault("unknown")
        val dirty = runCatching { git("status", "--porcelain", "--untracked-files=no").isNotEmpty() }
            .getOrDefault(false)

        val effectiveVersion = "$baseVersion.$patch"
        val packageDir = outputDir.get().asFile.resolve("com/oddlabs/util")
        packageDir.mkdirs()
        packageDir.resolve("BuildInfo.java").writeText(
            """
            package com.oddlabs.util;

            public final class BuildInfo {
                public static final String BASE_VERSION = "$baseVersion";
                public static final String VERSION = "$effectiveVersion";
                public static final String FULL_VERSION = "v" + VERSION + "-" + Compatibility.API_VERSION + "." + Compatibility.SIM_VERSION;
                public static final String GIT_COMMIT = "$commit";
                public static final boolean GIT_DIRTY = $dirty;
                public static final String DESCRIPTION = FULL_VERSION + " (" + GIT_COMMIT + (GIT_DIRTY ? "-dirty" : "") + ")";
                private BuildInfo() {}
            }

            """.trimIndent()
        )
    }
}

sourceSets.main {
    java.srcDir(generateBuildInfo)
}