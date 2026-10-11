plugins {
    java
    application
}

application {
    applicationName = "matchmaker"
    mainClass.set("com.oddlabs.matchserver.MatchmakingServer")
    applicationDefaultJvmArgs = listOf("-Djdk.crypto.KeyAgreement.legacyKDF=true")
}

// Fix start scripts to cd into APP_HOME so they find server.properties
fun fixStartScript(script: File) {
    var content = script.readText()
    if (script.name.endsWith(".bat")) {
        content = content.replace("@rem Execute ", "cd /d \"%APP_HOME%\"\r\n\r\n@rem Execute ")
    } else {
        content = content.replace("exec ", "cd \"\$APP_HOME\"\n\nexec ")
    }
    script.writeText(content)
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        outputDir?.listFiles()?.forEach { fixStartScript(it) }
    }
}

tasks.register<CreateStartScripts>("routerScripts") {
    mainClass.set("com.oddlabs.routerserver.RouterServer")
    applicationName = "router"
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    doLast {
        outputDir?.listFiles()?.filter { it.name.startsWith("router") }?.forEach { fixStartScript(it) }
    }
}

// Generate start-all scripts
val generateStartAll by tasks.registering {
    val outputDir = layout.buildDirectory.dir("scripts")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()

        dir.resolve("start.sh").apply {
            writeText(
                """
                |#!/bin/bash
                |cd "${'$'}(dirname "${'$'}0")"
                |chmod +x bin/* 2>/dev/null
                |
                |case "${'$'}1" in
                |  start)
                |    if pgrep -f "oddlabs.*(matchserver|routerserver)" > /dev/null 2>&1; then
                |      echo "Killing existing servers..."
                |      pkill -f "oddlabs.*(matchserver|routerserver)" 2>/dev/null
                |      sleep 1
                |    fi
                |    echo "Starting matchmaker..."
                |    nohup ./bin/matchmaker > logs/matchmaker.out 2>&1 &
                |    echo "Starting router..."
                |    nohup ./bin/router > logs/router.out 2>&1 &
                |    echo "Both servers started."
                |    ;;
                |  stop)
                |    echo "Stopping servers..."
                |    pkill -f "oddlabs.*(matchserver|routerserver)" 2>/dev/null
                |    echo "Servers stopped."
                |    ;;
                |  status)
                |    pgrep -fa "oddlabs.*(matchserver|routerserver)" || echo "No servers running."
                |    ;;
                |  *)
                |    echo "Usage: ${'$'}0 {start|stop|status}"
                |    exit 1
                |    ;;
                |esac
            """.trimMargin() + "\n"
            )
            setExecutable(true)
        }

        dir.resolve("start.bat").apply {
            writeText(
                """
                |@echo off
                |if "%1"=="start" (
                |    echo Starting matchmaker...
                |    start "" "%~dp0bin\matchmaker.bat"
                |    echo Starting router...
                |    start "" "%~dp0bin\router.bat"
                |    echo Both servers started.
                |) else if "%1"=="stop" (
                |    echo Stopping servers...
                |    taskkill /f /fi "WINDOWTITLE eq matchmaker*" 2>nul
                |    taskkill /f /fi "WINDOWTITLE eq router*" 2>nul
                |    echo Servers stopped.
                |) else (
                |    echo Usage: %0 {start^|stop}
                |)
            """.trimMargin() + "\r\n"
            )
        }
    }
}

tasks.installDist {
    dependsOn("routerScripts")
    doLast {
        destinationDir.resolve("bin").listFiles()?.filter { !it.name.endsWith(".bat") }?.forEach { it.setExecutable(true) }
        destinationDir.resolve("logs").mkdirs()
    }
}

tasks.distZip {
    dependsOn("routerScripts")
}

tasks.distTar {
    dependsOn("routerScripts")
}

distributions {
    main {
        distributionBaseName.set("server")
        contents {
            from("server.properties.template") {
                rename { "server.properties" }
            }
            from(generateStartAll)
        }
    }
}


dependencies {
    implementation(project(":common"))
    implementation("commons-pool:commons-pool:1.2")
    implementation("commons-dbcp:commons-dbcp:1.2.1")
    implementation("commons-collections:commons-collections:3.1")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("com.discord4j:discord4j-core:3.3.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.pocketcombats:openskill:1.1")
}

tasks.register<JavaExec>("runMatchmaker") {
    group = "application"
    description = "Run the matchmaking server"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.oddlabs.matchserver.MatchmakingServer")
    jvmArgs("-Djdk.crypto.KeyAgreement.legacyKDF=true")
}

tasks.register<JavaExec>("runRouter") {
    group = "application"
    description = "Run the router server"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.oddlabs.routerserver.RouterServer")
}
