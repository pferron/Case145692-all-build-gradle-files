plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-seeq-to-seeq"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine")
}

coverage {
    threshold.set(0.75)
}