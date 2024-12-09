plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.lombok
}

group = "com.seeq.utilities"
description = "seeq-test-utilities"

dependencies {
    testFixturesApi(platform(project(":seeq-platform")))
    testFixturesApi(project(":seeq-utilities"))
    testFixturesApi("junit:junit")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesImplementation("org.assertj:assertj-core")
    testFixturesImplementation("org.mockito:mockito-core")
    testFixturesImplementation("io.github.microutils:kotlin-logging")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.junit.platform:junit-platform-launcher")

    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("com.github.tomakehurst:wiremock-jre8")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
}

java {
    // TODO CRAB-42075: Remove when Ignition 7.9 support is deprecated
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            // ignition 7 module requires JDK8 source compatibility, which now throws a warning
            "-Xlint:-options",
        ),
    )
}