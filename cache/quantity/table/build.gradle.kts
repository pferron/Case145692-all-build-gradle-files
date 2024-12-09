plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(project(":cache:cache-quantity-interfaces"))
    api(project(":compute:data"))
    api(project(":seeq:common-monitoring"))
    implementation(project(":cache:cache-pipeline-quantity"))
    implementation(project(":seeq:common-serialization-seriesdata"))

    testImplementation(testFixtures(project(":seeq:common-monitoring")))
    testImplementation(testFixtures(project(":seeq:common-concurrent")))
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}