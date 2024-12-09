plugins {
    com.seeq.build.cache.`cache-module`
    com.seeq.build.docker.`compose`
    com.seeq.build.docker.`docker-application`
    com.seeq.build.helm.chart
    com.seeq.build.logback
    com.seeq.build.performance.jmh
}

dependencies {
    implementation(project(":cache:cache-persistent-debug-contract"))
    implementation(project(":cache:cache-persistent-service-server"))
    implementation(project(":cache:cache-persistent-service-archive"))
    implementation(project(":seeq:common-seeq-monitors"))
    implementation("com.github.jasync-sql:jasync-postgresql")
    runtimeOnly("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0") {
        because("Adds wrapping appenders that log trace_id and span_id")
    }

    api("io.opentelemetry:opentelemetry-api")

    testFixturesApi(testFixtures(project(":cache:cache-persistent-testfixtures")))
    testFixturesApi(project(":cache:cache-persistent-client"))
    testFixturesApi(testFixtures(project(":seeq-test-utilities")))
    testImplementation("io.kotest:kotest-assertions-core-jvm")
}

application {
    mainClass.set("com.seeq.cache.service.series.MainKt")
}

tasks {
    dockerBuild {
        imageName.set("series-cache")
    }
}