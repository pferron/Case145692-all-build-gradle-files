plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-drivers-signal-file"

dependencies {
    api(project(":seriesdata"))
    implementation(project(":appserver:appserver-corelib"))
    implementation(project(":seeq:common-compression"))
    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation("org.apache.commons:commons-collections4")
    implementation("org.apache.commons:commons-text")

    testImplementation(testFixtures(project(":appserver:appserver-corelib")))
}

coverage {
    threshold.set(0.78)
}