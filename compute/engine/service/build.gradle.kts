plugins {
    com.seeq.build.compute.`compute-service-module`
    com.seeq.build.docker.compose
    com.seeq.build.docker.`docker-application`
    com.seeq.build.helm.chart
    id("org.jetbrains.kotlin.plugin.allopen")
    id("io.micronaut.minimal.application")
    com.seeq.build.performance.jmh
}

dependencies {
    implementation(project(":appserver:appserver-analytics"))
    implementation(project(":appserver:appserver-compiler"))
    implementation(project(":appserver:appserver-grpc"))
    implementation(project(":cache:cache-persistent-client"))
    implementation(project(":cache:cache-pipeline-series"))
    implementation(project(":cache:cache-pipeline-quantity"))
    implementation(project(":cache:cache-quantity-scalar"))
    implementation(project(":cache:cache-quantity-table"))
    implementation(project(":compute:compute-engine-contract"))
    implementation(project(":compute:compute-engine-debug-contract"))
    implementation(project(":datasource-proxy:datasource-proxy-client"))
    implementation(project(":seeq:common-grpc-health"))
    implementation(project(":seeq:common-grpc-streaming-utils"))
    implementation(project(":seeq:common-grpc-unhandled-exception"))
    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation(project(":seeq:common-open-telemetry"))
    implementation(project(":seeq:common-seeq-monitors"))
    implementation("ch.qos.logback:logback-core:1.4.14") {
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }
    implementation("ch.qos.logback:logback-classic:1.4.14") {
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }
    implementation("net.logstash.logback:logstash-logback-encoder") {
        because("Output JSON-formatted structured logs")
    }
    runtimeOnly("org.yaml:snakeyaml")

    jmhImplementation("io.micronaut:micronaut-runtime")
    jmhImplementation(project(":compute:compute-engine-contract"))
    jmhImplementation(project(":cache:cache-persistent-contract"))?.because("Cache service stubs")

    testFixturesApi("io.grpc:grpc-testing")
    testImplementation(project(":seeq:common-serialization-seriesdata"))
    testImplementation(project(":seriesdata"))
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation(testFixtures(project(":seeq:common-monitoring")))
    testImplementation(project(":appserver:appserver-compute"))?.because("ComputeClient")
    testImplementation(project(":compute:compute-serialization-onnx"))
    testFixturesApi(testFixtures(project(":appserver:appserver-compiler")))?.because(
        "FormulaRunner for PipelinesFuzzContractTest",
    )
}

application {
    mainClass.set("com.seeq.compute.engine.MainKt")
}

micronaut {
    version("4.2.0")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.seeq.compute.engine.*")
    }
}

tasks {
    dockerBuildContext {
        from(projectDir) {
            include("container-entrypoint.sh")
        }
    }
    dockerBuild {
        imageName.set("compute")
    }
}