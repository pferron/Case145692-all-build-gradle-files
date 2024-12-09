plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-tables"

dependencies {
    implementation(project(":seriesdata"))
    implementation(project(":appserver:appserver-items"))
    implementation(project(":appserver:appserver-messaging"))
    implementation("cloud.pluses:ktypes:1.1.0")?.because("Reflection library used to get generic types at runtime")
    implementation("org.reflections:reflections")?.because("Reflection library used to get generic types at runtime")

    testImplementation(project(":appserver:appserver-drivers-graph"))
    testImplementation(project(":appserver:appserver-jobs"))
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation(testFixtures(project(":appserver:appserver-corelib")))
    testImplementation(testFixtures(project(":appserver:appserver-items")))
    testImplementation(testFixtures(project(":appserver:appserver-server")))
    testImplementation(testFixtures(project(":appserver:appserver-drivers-graph")))
    testFixturesApi(testFixtures(project(":appserver:appserver-query")))
}

coverage {
    threshold.set(0.7)
}