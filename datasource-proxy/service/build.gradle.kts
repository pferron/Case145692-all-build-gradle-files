plugins {
    com.seeq.build.datasourceproxy.`datasource-proxy-module`
    com.seeq.build.docker.`docker-application`
    com.seeq.build.helm.chart
    com.seeq.build.logback
    id("org.jetbrains.kotlin.plugin.allopen")
    id("io.micronaut.minimal.application")
}

micronaut {
    version("4.2.0")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.seeq.datasourceproxy.service.*")
    }
}

application {
    mainClass.set("com.seeq.datasourceproxy.service.MainKt")
}

dependencies {
    val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
    // TODO CRAB-48283 revisit this dependency in a followup PR
    api(project(":compute:compute-serialization-onnx"))
    implementation(project(":datasource-proxy:datasource-proxy-contract"))
    implementation(project(":datasource-proxy:datasource-proxy-core"))
    implementation(project(":seeq:common-data-coordinates"))
    implementation(project(":seeq:common-concurrent")) {
        because("needed for DatasourceProxyCoreWebSocket")
    }

    implementation(project(":seeq:common-grpc-unhandled-exception"))
    implementation(project(":seeq:common-grpc-streaming-utils"))

    implementation(project(":seeq:common-serialization-seriesdata"))

    implementation(project(":seeq:common-seeq-monitors"))

    implementation(project(":seeq-sdk")) {
        because(
            "As a temporary solution, the Datasource Proxy is querying data from appserver using the SDK. This " +
                "will be removed in CRAB-29206",
        )
    }

    implementation(project(":seeq-utilities")) {
        because("SeeqNames")
    }

    implementation(project(":seriesdata"))
    testImplementation(testFixtures(project(":seriesdata")))

    implementation(project(":seeq:common-open-telemetry"))
    runtimeOnly("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0") {
        because("Adds wrapping appenders that log trace_id and span_id")
    }

    implementation("com.github.jasync-sql:jasync-postgresql")
    implementation("ch.qos.logback:logback-core:1.4.14") {
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }
    implementation("ch.qos.logback:logback-classic:1.4.14") {
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }
    implementation("io.grpc:grpc-netty-shaded")

    implementation("io.micronaut:micronaut-jackson-databind")
    ksp("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-http-server-netty")
        ?.because("An HTTP/1 server is needed for the agent to establish a websocket connection")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.session:micronaut-session")
    implementation("io.micronaut.grpc:micronaut-grpc-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut.grpc:micronaut-grpc-annotation")
        ?.because(
            "needed for @GrpcChannel annotation so that we can create a special channel that talks to a " +
                "specific gRPC service",
        )

    implementation("io.reactivex.rxjava3:rxjava:3.1.2")
    implementation("com.google.protobuf:protobuf-java-util")

    testImplementation(project(":datasource-proxy:datasource-proxy-interfaces"))
    testImplementation(project(":datasource-proxy:datasource-proxy-client")) {
        because("Needed to test the gRPC client")
    }
    testImplementation(project(":seeq-utilities"))
    testImplementation(project(":seeq:common-seeq-monitors"))

    testImplementation("io.kotest:kotest-assertions-core-jvm")

    kspTest("io.micronaut.jaxrs:micronaut-jaxrs-processor") {
        because("Needed for appserver stub")
    }
    testImplementation("io.micronaut.jaxrs:micronaut-jaxrs-server") {
        because("Needed for appserver stub")
    }

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.yaml:snakeyaml")
}

tasks {
    compileTestKotlin {
        kotlinOptions.freeCompilerArgs =
            kotlinOptions.freeCompilerArgs + listOf("-opt-in=kotlin.io.path.ExperimentalPathApi")
    }
    distTar {
        enabled = false
    }
    distZip {
        enabled = false
    }
}

tasks {
    dockerBuild {
        imageName.set("datasource-proxy")
    }
}