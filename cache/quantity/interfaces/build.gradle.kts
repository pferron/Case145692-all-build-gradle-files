plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(project(":seriesdata")) { because("Uncertain, QueryParameters") }
    api("com.github.ben-manes.caffeine:caffeine") { because("Ticker") }

    testImplementation(testFixtures(project(":seeq:common-monitoring")))
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}