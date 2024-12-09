plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-drivers-condition"

dependencies {
    api(project(":seriesdata"))
    implementation(project(":appserver:appserver-corelib"))
    implementation(project(":appserver:appserver-drivers-graph"))

    testImplementation(testFixtures(project(":appserver:appserver-corelib")))
    testImplementation(testFixtures(project(":appserver:appserver-server")))

    testImplementation("org.assertj:assertj-core")
}

coverage {
    threshold.set(0.51)
}