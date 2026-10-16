plugins {
    java
    application
}

application {
    applicationName = "bugserver"
    mainClass.set("com.oddlabs.bugserver.BugReportServer")
}

dependencies {
    implementation(project(":common"))
}

distributions {
    main {
        distributionBaseName.set("bugserver")
    }
}

tasks.named<JavaExec>("run") {
    // Local testing: listen on localhost and keep reports inside the build dir.
    systemProperty("bugserver.dir", layout.buildDirectory.dir("reports").get().asFile.absolutePath)
}
