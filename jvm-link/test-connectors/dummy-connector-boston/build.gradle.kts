plugins {
    com.seeq.build.link.jvm.connector
}

dependencies {
    implementation(project(":jvm-link:dummy-library-mood-ecstatic"))
}

group = "com.dummycorp.seeq.link.connector"
description = "dummycorp-seeq-link-connector-boston"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector