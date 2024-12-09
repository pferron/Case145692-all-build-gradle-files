plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-influxdb"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("org.influxdb:influxdb-java:2.23")
    implementation("com.squareup.okhttp3:okhttp:4.11.0") {
        because("CRAB-38460")
    }
    implementation("com.squareup.okio:okio:3.4.0") {
        because("CRAB-38460")
    }
}

coverage {
    threshold.set(0.21)
}