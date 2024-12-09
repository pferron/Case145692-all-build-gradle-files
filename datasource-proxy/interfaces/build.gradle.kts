plugins {
    com.seeq.build.datasourceproxy.`datasource-proxy-module`
}

dependencies {
    api(project(":seeq:common-open-telemetry")) {
        because("Define OpenTelemetry monitors")
    }

    implementation(project(":seeq-utilities"))
    implementation(project(":seeq:common-monitoring")) {
        because("Pass monitors back to appserver")
    }

    implementation("javax.inject:javax.inject")
    implementation("com.github.ben-manes.caffeine:caffeine")?.because("AuthenticationRateLimiter")

    testImplementation(testFixtures(project(":seeq:common-concurrent")))
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
}