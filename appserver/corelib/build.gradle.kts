plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-corelib"

dependencies {
    api(project(":seeq:common-monitoring"))
    api(project(":seeq:common-seeq-monitors"))
    api(project(":seeq:common-activity"))
    api(project(":seriesdata"))

    api("org.jooq:jooq") {
        because("PostgresUtils and SeeqExceptions catch jOOQ exceptions")
    }

    implementation(project(":seeq:common-concurrent"))
    implementation("org.bouncycastle:bcpkix-jdk18on")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.postgresql:postgresql") {
        because("DbManager checks if PostgreSQL started")
    }
    implementation("com.google.guava:guava")
    implementation("org.slf4j:slf4j-api")

    testImplementation("org.reflections:reflections")
    testImplementation("io.kotest:kotest-assertions-core-jvm")

    testFixturesApi("org.flywaydb:flyway-core:8.5.13")
    testFixturesApi(testFixtures(project(":seriesdata")))
    testFixturesApi("com.github.ben-manes.caffeine:caffeine")
    testFixturesApi("com.opentable.components:otj-pg-embedded")
}

coverage {
    threshold.set(0.40)
    excludes.addAll(
        "**/forked/com/google/common/reflect/**/*.class",
        "**/mocks/**/*.class",
        "**/appserver/test/**/*.class",
        "**/*Container.class",
    )
}

// TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:-this-escape",
        ),
    )
}