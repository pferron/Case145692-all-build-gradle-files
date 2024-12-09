plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.protobuf
}

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":appserver:appserver-corelib"))

    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.google.guava:guava")

    testImplementation(testFixtures(project(":seeq-test-utilities")))
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

coverage {
    threshold.set(0.91)
    excludes.add("**/authentication/serialization/*.class")
}