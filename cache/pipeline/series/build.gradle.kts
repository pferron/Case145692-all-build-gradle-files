plugins {
    com.seeq.build.appserver.`appserver-module`
    com.seeq.build.performance.jmh
}

description = "appserver-pipeline"

dependencies {
    api(project(":seriesdata"))?.because("QueryParameters, Sample, Capsule etc used in public interface")
    api(project(":seeq:common-data-coordinates"))
    api(project(":compute:data"))

    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation(project(":seeq:common-monitoring"))
    implementation(project(":seeq:common-seeq-monitors"))
    implementation(project(":cache:cache-persistent-client"))
    implementation(project(":cache:cache-persistent-contract")) {
        because("Pipelines' MonitorRegistry needs access to the cache service's MonitorRegistry")
    }
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation(project(":seeq:common-open-telemetry"))
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations")

    testImplementation(testFixtures(project(":seeq-test-utilities")))
    testImplementation(testFixtures(project(":appserver:appserver-corelib")))
    testImplementation(testFixtures(project(":seriesdata")))
    testImplementation(testFixtures(project(":compute:data")))
    testImplementation(testFixtures(project(":appserver:appserver-compiler"))) {
        because("Generated series for fuzz testing")
    }
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation(testFixtures(project(":seriesdata")))
    jmhImplementation(testFixtures(project(":seriesdata")))
    jmhImplementation(testFixtures(project(":compute:data")))
    jmhImplementation(project(":seeq:common-serialization-seriesdata"))
    jmhImplementation(project(":seeq:common-seeq-monitors"))
    jmhImplementation(project(":seeq:common-open-telemetry")) {
        because(
            "data.pipeline.microbenchmarks.Utils calls BatchSource's constructor and throws a compilation error" +
                "if this is not included.",
        )
    }
}

coverage {
    threshold.set(0.70)
}