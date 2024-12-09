group = "com.seeq.monitoring"

plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api("javax.inject:javax.inject")
    api("io.dropwizard.metrics:metrics-core")

    implementation("javax.annotation:javax.annotation-api")
    implementation("com.google.guava:guava")
    implementation("io.bretty:console-table-builder:1.2")

    testImplementation(testFixtures(project(":seeq-test-utilities")))
    testImplementation(project(":seeq:common-concurrent"))
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")

    testFixturesApi("junit:junit")
    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")
    testFixturesApi("org.assertj:assertj-core")
}

coverage {
    threshold.set(0.80)
}