import com.smushytaco.lwjgl_gradle.Module

plugins {
    application
    id("com.smushytaco.lwjgl3")
}

lwjgl {
    version = "3.4.1"
    implementation(
        Module.CORE,
        Module.GLFW,
        Module.OPENAL,
        Module.OPENGL,
        Module.STB,
        Module.TINYFD
    )
}

application {
    mainClass.set("com.oddlabs.tt.Main")
    val args = mutableListOf(
        "-ea", "-esa",
        "--enable-native-access=ALL-UNNAMED",
        "-Dcom.oddlabs.tt.developer=true",
        "-Djdk.crypto.KeyAgreement.legacyKDF=true",
        "-Xms80m", "-Xmx512m"
    )
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        args.add("-XstartOnFirstThread")
        args.add("-Xdock:name=TribalTrouble")
        args.add("-Xdock:icon=" + project.projectDir.resolve("icons/DesktopIconImage.png").absolutePath)
    }
    applicationDefaultJvmArgs = args
}

dependencies {
    implementation(project(":common"))
    implementation(project(":assets"))
    implementation("com.code-disaster.steamworks4j:steamworks4j:1.10.0")
}

// steamworks4j lacks module-info, so it stays on the classpath as the unnamed module.
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("--add-reads", "com.oddlabs.tt=ALL-UNNAMED"))
}

apply(from = "translations.gradle.kts")

// --- Revision ---

val revision = tasks.register("revision") {
    val output = layout.buildDirectory.file("revision_number")
    outputs.file(output)
    outputs.upToDateWhen { false }
    doLast {
        output.get().asFile.apply {
            parentFile.mkdirs()
            writeText("gradle-build")
        }
    }
}

tasks.processResources {
    inputs.files(revision)
    from(rootProject.file("CHANGELOG.md")) { into("com/oddlabs/tt/form") }
}

tasks.run.configure {
    classpath = files(layout.buildDirectory) + sourceSets.main.get().runtimeClasspath
}

// --- Distribution & Packaging ---

val dist = layout.buildDirectory.dir("dist")
val distCommon = dist.map { it.dir("common") }
val mainJarFile = tasks.jar.flatMap { it.archiveFileName }

val stageDist by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Assemble all files for jpackage into dist/common"
    dependsOn(":assets:textures", ":assets:geometry", "revision")

    into(distCommon)

    from(tasks.jar)
    from(configurations.runtimeClasspath)

    from(project(":assets").layout.buildDirectory.dir("textures")) {
        into("textures")
    }
    from(project(":assets").layout.buildDirectory.dir("geometry")) {
        into("geometry")
    }
    from(layout.buildDirectory.file("revision_number"))
}

// jpackage --app-content places files at the install root (next to app/ and runtime/),
// where the runtime expects to find icons/DesktopIconImage.png relative to CWD.
val stageAppContent by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Stage files for jpackage --app-content at the install root"
    into(dist.map { it.dir("app-content") })
    from("icons/DesktopIconImage.png") {
        into("icons")
    }
}

val packageWindows by tasks.registering(Exec::class) {
    group = "distribution"
    description = "Build Windows app-image via jpackage"
    dependsOn(stageDist, stageAppContent)
    onlyIf { System.getProperty("os.name").lowercase().contains("windows") }

    val destDir = dist.map { it.dir("windows") }
    val appContentDir = dist.map { it.dir("app-content") }

    doFirst {
        destDir.get().asFile.resolve("TribalTrouble").deleteRecursively()
    }

    workingDir(project.projectDir)
    executable("jpackage")
    args(
        "--name", "TribalTrouble",
        "--icon", project.projectDir.resolve("icons/TribalTrouble.ico").absolutePath,
        "--dest", destDir.get().asFile.absolutePath,
        "--input", distCommon.get().asFile.absolutePath,
        "--app-content", appContentDir.get().asFile.resolve("icons").absolutePath,
        "--main-jar", mainJarFile.get(),
        "--main-class", "com.oddlabs.tt.Main",
        "--java-options",
        "-ea -Djdk.crypto.KeyAgreement.legacyKDF=true -Xmx512m -cp \$APPDIR\\;\$APPDIR\\*",
        "--type", "app-image",
    )
}

// macOS jpackage helper
fun registerMacPackage(
    taskName: String,
    destSubdir: String,
    jpackageType: String,
): TaskProvider<Exec> = tasks.register<Exec>(taskName) {
    group = "distribution"
    description = "Build macOS $jpackageType into $destSubdir via jpackage"
    dependsOn(stageDist)
    onlyIf { System.getProperty("os.name").lowercase().contains("mac") }

    val destDir = dist.map { it.dir(destSubdir) }

    workingDir(project.projectDir)
    executable("jpackage")
    args(
        "--name", "TribalTrouble",
        "--icon", project.projectDir.resolve("icons/TribalTrouble.icns").absolutePath,
        "--dest", destDir.get().asFile.absolutePath,
        "--input", distCommon.get().asFile.absolutePath,
        "--main-jar", mainJarFile.get(),
        "--main-class", "com.oddlabs.tt.Main",
        "--java-options",
        // -Xdock:icon/-Xdock:name ensure the dock icon is set when the inner launcher is invoked
        // directly (e.g. by Steam) and LaunchServices doesn't read the bundle Info.plist for us.
        "-ea -XstartOnFirstThread -cp \$APPDIR:\$APPDIR/* -Djdk.crypto.KeyAgreement.legacyKDF=true " +
                "-Xdock:name=TribalTrouble -Xdock:icon=\$APPDIR/../Resources/TribalTrouble.icns",
        "--type", jpackageType,
    )
}

val packageMacX86 = registerMacPackage("packageMacX86", "macosx-x86", "dmg")
val packageMacArm64 = registerMacPackage("packageMacArm64", "macosx-arm64", "dmg")
val packageMacArm64App = registerMacPackage("packageMacArm64App", "macosx-arm64-app", "app-image")
val packageMacX86App = registerMacPackage("packageMacX86App", "macosx-x86-app", "app-image")

val packageLinux by tasks.registering {
    group = "distribution"
    description = "Build Linux AppImage via jlink + appimagetool"
    dependsOn(stageDist)
    onlyIf { System.getProperty("os.name").lowercase().contains("linux") }

    val linuxDist = dist.map { it.dir("linux") }
    val appDir = linuxDist.map { it.dir("TribalTrouble.AppDir") }

    doLast {
        val appDirFile = appDir.get().asFile
        val linuxDistFile = linuxDist.get().asFile

        appDirFile.deleteRecursively()
        appDirFile.mkdirs()
        appDirFile.resolve("usr/lib").mkdirs()

        val appImageSrc = project.projectDir.resolve("linux/TribalTrouble.AppDir")
        listOf("AppRun", "icon.png", "TribalTrouble.desktop").forEach { f ->
            appImageSrc.resolve(f).copyTo(appDirFile.resolve(f), overwrite = true)
        }
        appDirFile.resolve("AppRun").setExecutable(true)

        distCommon.get().asFile.copyRecursively(appDirFile.resolve("common"), overwrite = true)

        val toolSrc = project.projectDir.resolve("linux/appimagetool-x86_64.AppImage")
        val toolDest = linuxDistFile.resolve("appimagetool-x86_64.AppImage")
        toolSrc.copyTo(toolDest, overwrite = true)
        toolDest.setExecutable(true)

        val jlink = ProcessBuilder(
            "jlink",
            "--output", appDirFile.resolve("usr/lib/jre").absolutePath,
            "--add-modules",
            "java.base,java.desktop,java.prefs,java.rmi,java.sql,jdk.unsupported",
            "--compress=2",
            "--no-header-files",
            "--no-man-pages",
            "--strip-debug",
        ).directory(linuxDistFile).inheritIO().start()
        if (jlink.waitFor() != 0) throw GradleException("jlink failed")

        val appimage = ProcessBuilder(
            "./appimagetool-x86_64.AppImage", "TribalTrouble.AppDir"
        ).directory(linuxDistFile).inheritIO().start()
        if (appimage.waitFor() != 0) throw GradleException("appimagetool failed")

        linuxDistFile.resolve("TribalTrouble-x86_64.AppImage").setExecutable(true)
    }
}
