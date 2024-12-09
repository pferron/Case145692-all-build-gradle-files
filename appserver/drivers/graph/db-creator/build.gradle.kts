plugins {
    id("com.seeq.build.kotlin-module")
    kotlin("plugin.serialization")
}
description = "TestDBCreator"

dependencies {
    implementation(testFixtures(project(":appserver:appserver-corelib")))
    implementation(project(":appserver:appserver-drivers-graph"))
    implementation(project(":appserver:appserver-drivers-condition"))
    implementation(project(":appserver:appserver-items"))
    implementation("com.github.ajalt.clikt:clikt")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation(testFixtures(project(":seeq:common-monitoring")))
}

tasks.register<JavaExec>("runTestDbCreator") {
    classpath = sourceSets["testFixtures"].runtimeClasspath
    mainClass.set("com.seeq.appserver.driver.graph.db.creator.Main")
}