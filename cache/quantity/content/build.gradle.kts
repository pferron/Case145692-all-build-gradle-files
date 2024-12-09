plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(project(":cache:cache-quantity-interfaces"))
    api(project(":seeq:common-monitoring"))
    implementation(project(":cache:cache-pipeline-quantity"))

    testImplementation(testFixtures(project(":seeq:common-monitoring")))
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}