plugins {
    com.seeq.build.link.jvm.connector
}

dependencies {
    implementation(project(":jvm-link:dummy-library-mood-happy"))
}

group = "com.dummycorp.seeq.link.connector"
description = "dummycorp-seeq-link-connector-atlanta"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector