group = "com.seeq.json"

plugins {
    com.seeq.build.`java-module`
    com.seeq.build.sdk.`connector-sdk-part`
}

dependencies {
    api(platform(project(":seeq-platform")))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}