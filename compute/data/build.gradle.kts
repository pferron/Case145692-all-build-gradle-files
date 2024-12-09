plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.performance.jmh
}

group = "com.seeq"
description = "compute-data"

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":seriesdata"))

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("com.google.guava:guava-testlib")
    testImplementation(testFixtures(project(":seeq-test-utilities")))
    testFixturesApi(testFixtures(project(":seriesdata")))
}

coverage {
    threshold.set(0.41)
    excludes.addAll("com/seeq/forked/*")
}

tasks {
    // TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:-this-escape",
            ),
        )
    }
}