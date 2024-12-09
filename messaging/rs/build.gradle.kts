plugins {
    com.seeq.build.messaging.`messaging-module`
}

group = "com.seeq.messaging"
description = "messaging-rs"

dependencies {
    api(project(":messaging:messaging-core"))
    api(project(":messaging:messaging-databind"))

    api("io.swagger:swagger-jersey2-jaxrs:1.5.16")
    api("jakarta.ws.rs:jakarta.ws.rs-api")

    api("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider")
    api("org.glassfish.jersey.media:jersey-media-json-jackson")

    api("javax.inject:javax.inject")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testFixturesApi("junit:junit")
    testFixturesApi(kotlin("test-junit"))

    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")

    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")
}