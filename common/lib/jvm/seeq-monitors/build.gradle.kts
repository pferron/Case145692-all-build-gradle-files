plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":seeq:common-monitoring"))

    testImplementation("junit:junit")
    testImplementation("org.assertj:assertj-core")
}