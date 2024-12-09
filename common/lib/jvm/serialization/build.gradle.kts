group = "com.seeq.serialization.signal"
description = "common-serialization"

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.performance.jmh
    com.seeq.build.protobuf
    com.seeq.build.sdk.`connector-sdk-part`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":seeq:common-compression"))

    implementation("io.airlift:aircompressor:0.27")
    implementation("com.google.guava:guava")
    implementation("commons-lang:commons-lang:2.6")
    implementation("com.google.protobuf:protobuf-java")

    testFixturesImplementation("org.assertj:assertj-core")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

tasks {
    sourcesJar {
        // Maven Central requires a source JAR, but we don't want to publish any sources for this library
        exclude("**/*")
    }
}

coverage {
    threshold.set(0.81)
}