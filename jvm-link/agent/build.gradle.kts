plugins {
    com.seeq.build.link.jvm.`link-module`
    com.seeq.build.`kotlin-module` // To enable Kotlin tests only. Production Kotlin is disabled.
    com.seeq.build.docker.`docker-application`
    com.seeq.build.sdk.`connector-sdk-part`
}

description = "seeq-link-agent"

// The JVM Agent version is defined to be identical to the JVM Link SDK version and has the format
//     {major}.{minor}.{patchMajor}.{patchMinor}{suffix}
//     |_____________||________________________||______|
//            |                    |               |
//         Manually       Generated from the     Set by
//        defined in     current timestamp in  the build
//     `variables.ini`      `variables.py`       flags
version = "${project.properties["linkSdkMajorVersion"]}.${project.properties["linkSdkMinorVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

val connectorProjects = project(":jvm-link").subprojects.filter {
    it.name.contains("connector") && !it.name.contains("dummy") && !it.name.contains("commons")
}
val connectorConfigurationMap = connectorProjects.associateBy({ it.path }, { configurations.create(it.name) })

val downloadJdbcDrivers by tasks.registering(com.seeq.build.ToolchainDownload::class) {
    filename.set("sql-server-jdbc-driver-dlls-9.4-64bit-universal.tar.gz")
}

dependencies {
    api(project(":jvm-link:seeq-link-sdk"))

    implementation(project(":seeq-utilities"))
    implementation("org.eclipse.jetty.websocket:websocket-client")
    implementation("javax.servlet:javax.servlet-api")
    implementation("commons-cli:commons-cli")
    implementation("org.apache.commons:commons-lang3")
    implementation("uk.org.lidalia:sysout-over-slf4j")
    implementation(project(":seeq:common-json"))
    implementation(project(":seeq:common-message"))
    implementation(project(":seeq:common-serialization"))
    implementation(project(":seeq-sdk"))
    implementation(project(":seeq-utilities"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations")
    implementation("org.glassfish.jaxb:jaxb-runtime") {
        because("We leaked this in the past and now connectors may depend on it")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
        because("We leaked this in the past and now connectors may depend on it")
    }

    // For WebSocketTestServer
    // not put into test scope, because ApiClient uses it and test scope results in ClassNotFound
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson")

    // For WebSocketTestServer
    testImplementation("org.glassfish.grizzly:grizzly-http-server")
    testImplementation("org.glassfish.grizzly:grizzly-http-servlet")
    testImplementation("org.glassfish.grizzly:grizzly-framework")
    testImplementation("org.glassfish.grizzly:grizzly-core")
    testImplementation("org.glassfish.grizzly:grizzly-websockets")
    testImplementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http")
    testImplementation("org.glassfish.jersey.inject:jersey-hk2")
    testImplementation("org.awaitility:awaitility")

    testImplementation(testFixtures(project(":jvm-link:seeq-link-sdk")))

    connectorConfigurationMap.forEach { (path, configuration) ->
        configuration(project(path, "image")) {
            because("we bundle this connector with jvm-link")
        }
    }
}

val extraConnectors = listOf(
    "seeq-link-connector-chaos-monkey",
    "seeq-link-connector-metnet",
    "seeq-link-connector-stress-test",
    "seeq-link-connector-xhq",
)

application {
    mainClass.set("com.seeq.link.agent.Main")
}

coverage {
    threshold.set(0.50)
}

tasks {
    // Production Kotlin is disabled in this project because we want to make sure all the production code is
    // written in Java to ensure near 100% parity with C#. However, in some cases, Kotlin tests are more convenient
    // or allow code reuse with other JVM projects, so we enable Kotlin tests only.
    compileKotlin {
        enabled = false
    }

    named<JavaExec>("run") {
        inputs.files(connectorConfigurationMap.values).withNormalizer(ClasspathNormalizer::class)
        workingDir = File(rootDir, "sq-run-data-dir")
        jvmArgs(
            // CRAB-28325: Reflective access is necessary to fix netty (used by Apache Arrow from SparkJDBC42
            // driver)
            "--add-opens=java.base/java.nio=ALL-UNNAMED", "-XX:+UseG1GC",

            "-Dlogback.configurationFile=\"${file("src/main/resources/logback.xml")}\"",
        )
    }
    dockerBuild {
        imageName.set("jvm-link")
    }
    dockerBuildContext {
        connectorConfigurationMap.values.forEach {
            from(it) {
                val dst = if (extraConnectors.contains(it.name)) "extras" else "connectors"
                into("$dst/${it.name.replace("seeq-link-connector-", "")}")
            }
        }
        from(tarTree(downloadJdbcDrivers.map { it.outputs.files.singleFile })) {
            into("sql-server-jdbc-driver-dlls")
        }
    }

    // TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:-this-escape",
            ),
        )
    }
}