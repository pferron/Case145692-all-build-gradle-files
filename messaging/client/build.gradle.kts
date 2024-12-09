plugins {
    com.seeq.build.messaging.`messaging-module`
}

group = "com.seeq.messaging"
description = "messaging-client"

dependencies {
    api(project(":messaging:messaging-core"))
    api(project(":messaging:messaging-databind"))
    api(project(":messaging:messaging-rs"))

    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("dev.failsafe:failsafe")
    implementation("com.github.luben:zstd-jni:1.5.0-4")

    testImplementation(project(":messaging:messaging-service-websocket"))
    testImplementation("org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-inmemory")
    testImplementation("org.glassfish.jersey.inject:jersey-hk2")

    api("com.google.inject:guice")
    implementation("javax.inject:javax.inject")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testFixturesApi("junit:junit")
    testFixturesApi(kotlin("test-junit"))

    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")

    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")
}