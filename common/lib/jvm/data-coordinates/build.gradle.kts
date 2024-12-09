group = "com.seeq.data-coordinates"

plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))
}