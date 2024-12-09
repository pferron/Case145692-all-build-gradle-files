plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-influxdb2"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.influxdb:influxdb-client-java:4.2.0")

    implementation(project(":jvm-link:seeq-link-connector-influxdb"))
    testImplementation(testFixtures(project(":jvm-link:seeq-link-connector-influxdb")))
}

coverage {
    threshold.set(0.0)
}