plugins {
    com.seeq.build.messaging.`messaging-module`
    com.seeq.build.performance.jmh
}

group = "com.seeq.messaging"
description = "messaging-service-websocket"

dependencies {
    api(project(":messaging:messaging-service-core"))
    api(project(":messaging:messaging-core"))
    api(project(":messaging:messaging-databind"))

    implementation("io.swagger:swagger-jersey2-jaxrs:1.5.16")
    implementation("org.glassfish.grizzly:grizzly-http-server")
    implementation("org.glassfish.grizzly:grizzly-http-servlet")
    implementation("org.glassfish.grizzly:grizzly-framework")
    implementation("org.glassfish.grizzly:grizzly-core")
    implementation("org.glassfish.grizzly:grizzly-websockets")
    implementation("org.glassfish.grizzly:grizzly-http2:2.4.4")
    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http")
    implementation("org.glassfish.jersey.inject:jersey-hk2")
    implementation("javax.servlet:javax.servlet-api")

    api("com.google.inject:guice")
    implementation("javax.inject:javax.inject")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.github.luben:zstd-jni:1.5.0-4")

    testFixturesApi("junit:junit")
    testFixturesApi(kotlin("test-junit"))

    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")

    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")

    jmhImplementation(project(":appserver:appserver-messaging"))
    jmhImplementation(project(":appserver:appserver-server"))
    jmhImplementation(project(":messaging:messaging-service-websocket"))
    jmhImplementation("org.glassfish.grizzly:grizzly-websockets")
}