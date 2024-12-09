plugins {
    com.seeq.build.messaging.`messaging-module`
}

group = "com.seeq.messaging"
description = "messaging-databind"

dependencies {
    api(project(":messaging:messaging-core"))

    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("junit:junit")
    testImplementation(kotlin("test-junit"))
}