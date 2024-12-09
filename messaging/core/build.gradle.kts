plugins {
    com.seeq.build.messaging.`messaging-module`
}

group = "com.seeq.messaging"
description = "messaging-core"

dependencies {
    api(project(":seeq:common-concurrent"))

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

coverage {
    threshold.set(0.20)
}