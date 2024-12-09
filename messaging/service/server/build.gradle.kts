plugins {
    com.seeq.build.messaging.`messaging-module`
    com.seeq.build.docker.`docker-application`
}

group = "com.seeq.messaging"
description = "messaging-service-server"

dependencies {
    api(project(":messaging:messaging-core"))
    api(project(":messaging:messaging-rs"))
    api(project(":messaging:messaging-service-core"))
    api(project(":messaging:messaging-service-websocket"))

    implementation(project(":seeq:common-concurrent")) {
        because("WebSocket uses a fixed size thread pool")
    }

    api(project(":seeq:common-tiny-config"))
    implementation(project(":seeq:common-tiny-config-reporter"))

    implementation("com.github.ben-manes.caffeine:caffeine") {
        because("cached metric trackers")
    }

    implementation("commons-cli:commons-cli")

    implementation("io.swagger:swagger-jersey2-jaxrs:1.5.16")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")

    implementation("org.glassfish.grizzly:grizzly-http-server")
    implementation("org.glassfish.grizzly:grizzly-http-servlet")
    implementation("org.glassfish.grizzly:grizzly-framework")
    implementation("org.glassfish.grizzly:grizzly-core")
    implementation("org.glassfish.grizzly:grizzly-websockets")
    implementation("org.glassfish.grizzly:grizzly-http2:2.4.4")
    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet")
    implementation("org.glassfish.jersey.inject:jersey-hk2")

    implementation("javax.servlet:javax.servlet-api")

    implementation("com.google.inject:guice")
    implementation("javax.inject:javax.inject")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testFixturesApi("junit:junit")
    testFixturesApi(kotlin("test-junit"))

    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")

    implementation("io.dropwizard.metrics:metrics-servlets")

    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")
    implementation("uk.org.lidalia:sysout-over-slf4j")
    implementation("org.slf4j:jul-to-slf4j")
}

application {
    mainClass.set("com.seeq.messaging.service.MainKt")
}

tasks {
    dockerBuild {
        imageName.set("messaging")
    }
}