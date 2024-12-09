group = "com.seeq.opentelemetry"

plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api("javax.inject:javax.inject")
    api("io.opentelemetry:opentelemetry-api")
    implementation("javax.annotation:javax.annotation-api")
    implementation("com.google.guava:guava")

    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")

    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")
    testFixturesApi("org.assertj:assertj-core")
}

coverage {
    threshold.set(0.70)
}