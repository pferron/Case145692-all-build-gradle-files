plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-noaa-weather"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.seeq.jscience:jscience")
}

coverage {
    threshold.set(0.51)
}