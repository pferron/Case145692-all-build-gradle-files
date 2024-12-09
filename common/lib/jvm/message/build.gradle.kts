group = "com.seeq.message"

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.sdk.`connector-sdk-part`
}

dependencies {
    api(platform(project(":seeq-platform")))
    testFixturesApi("org.assertj:assertj-core")
}