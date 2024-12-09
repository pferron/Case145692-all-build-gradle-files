group = "com.seeq.test-categories"
description = "common-test-categories"

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.sdk.`connector-sdk-part`
}

tasks {
    sourcesJar {
        // Maven Central requires a source JAR, but we don't want to publish any sources for this library
        exclude("**/*")
    }
}