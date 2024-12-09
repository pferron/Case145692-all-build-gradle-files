plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(project(":cache:cache-quantity-interfaces"))
    api("com.google.inject:guice")
    api("com.github.ben-manes.caffeine:caffeine")

    implementation(project(":seeq:common-monitoring"))
    implementation(project(":seeq:common-seeq-monitors"))
    implementation(project(":cache:cache-persistent-client"))
    implementation(project(":seeq-utilities"))
    implementation(project(":compute:data"))

    testImplementation(project(":seriesdata"))
    testImplementation(testFixtures(project(":seeq:common-concurrent")))
    testImplementation(testFixtures(project(":seeq:common-monitoring")))
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

coverage {
    threshold.set(0.62)
}