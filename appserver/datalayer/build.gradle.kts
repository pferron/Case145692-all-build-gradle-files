plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-datalayer"

dependencies {
    implementation(project(":appserver:appserver-corelib"))
    implementation(project(":appserver:appserver-items"))
    implementation(project(":appserver:appserver-drivers-graph"))
    implementation(project(":appserver:appserver-drivers-signal-file"))
    implementation(project(":appserver:appserver-jobs"))
    implementation(project(":appserver:appserver-messaging"))
    implementation(project(":cache:cache-pipeline-series"))

    implementation(project(":datasource-proxy:datasource-proxy-client"))

    testImplementation(project(":datasource-proxy:datasource-proxy-client"))
    testImplementation(testFixtures(project(":appserver:appserver-items")))
    testImplementation(testFixtures(project(":seriesdata")))
}

coverage {
    threshold.set(0.52)
    excludes.addAll("**/link/messages/**/*.class")
}