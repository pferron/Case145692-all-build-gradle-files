plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-omnia"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("com.squareup.okhttp3:okhttp:4.11.0") {
        because("CRAB-38460")
    }
    implementation("com.squareup.okio:okio:3.4.0") {
        because("CRAB-38460")
    }

    implementation("com.microsoft.azure:msal4j:1.13.3") {
        because("we need to use OAuth 2.0 for authentication")
    }

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}