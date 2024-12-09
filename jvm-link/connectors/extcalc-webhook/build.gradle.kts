plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-addoncalc-webhook"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    // TODO CRAB-48283 revisit this dependency in a followup PR (also implementation vs api)
    api(project(":compute:compute-serialization-onnx"))
    implementation("org.apache.commons:commons-collections4")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}