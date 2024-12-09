plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-file-folders"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    providedLibraries(project(":jvm-link:seeq-link-agent"))
    implementation("com.opencsv:opencsv")
    implementation("org.reflections:reflections")
}

coverage {
    threshold.set(0.62)
}