plugins {
    com.seeq.build.cache.`cache-module`
    com.seeq.build.performance.jmh
}

dependencies {
    api("com.google.inject:guice")

    implementation(project(":cache:cache-persistent-service-core"))
    implementation("net.openhft:zero-allocation-hashing")
    implementation("com.github.jasync-sql:jasync-postgresql")

    api("io.opentelemetry:opentelemetry-api")

    testImplementation(testFixtures(project(":cache:cache-persistent-testfixtures")))

    testFixturesApi("com.github.jasync-sql:jasync-postgresql")
    testFixturesApi("com.opentable.components:otj-pg-embedded")
    testFixturesApi(project(":cache:cache-persistent-service-core"))
}

tasks {
    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    counter = "COMPLEXITY"
                    minimum = 0.46.toBigDecimal()
                }
            }
        }
    }
}