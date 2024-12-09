plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-gpm"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}