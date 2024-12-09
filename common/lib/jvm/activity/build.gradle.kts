plugins {
    id("com.seeq.build.kotlin-module")
}

dependencies {
    api(platform(project(":seeq-platform")))
    // api("com.google.guava:guava")

    implementation("com.google.guava:guava")
    implementation("org.slf4j:slf4j-api")
    implementation("io.github.microutils:kotlin-logging")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}