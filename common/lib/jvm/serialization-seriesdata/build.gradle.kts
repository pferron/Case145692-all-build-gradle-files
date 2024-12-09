group = "com.seeq.serialization.seriesdata.signal"

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.performance.jmh
}

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":seeq:common-serialization"))
    api(project(":seriesdata"))
    testImplementation(testFixtures(project(":seriesdata")))
    testImplementation("com.opencsv:opencsv")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

coverage {
    threshold.set(0.81)
}