group = "com.seeq.concurrent"

plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))

    implementation("com.google.guava:guava")

    testImplementation("org.assertj:assertj-core")

    testFixturesApi("junit:junit")
    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi(kotlin("test-junit"))
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")
    testFixturesApi("com.github.ben-manes.caffeine:caffeine")
}

coverage {
    threshold.set(0.60)
}