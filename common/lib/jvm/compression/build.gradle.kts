group = "com.seeq.compression"
description = "common-compression"

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.sdk.`connector-sdk-part`
}

dependencies {
    api(platform(project(":seeq-platform")))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

tasks {
    sourcesJar {
        // Maven Central requires a source JAR, but we don't want to publish any sources for this library
        exclude("**/*")
    }
}