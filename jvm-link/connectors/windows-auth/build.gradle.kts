plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-windows-auth"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation(("com.github.waffle:waffle-jna:2.0.0")) {
        exclude(group = "org.slf4j")
    }
}

coverage {
    threshold.set(0.77)
}