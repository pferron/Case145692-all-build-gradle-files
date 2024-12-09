plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-cognite"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")

    implementation("com.nimbusds:oauth2-oidc-sdk:10.1")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.2")
    /** nimbusds libraries use this bouncycastle dependency as an optional dependency,
     * so its version must be compatible with nimbusds's specified versions
     * https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support
     */
    implementation("org.bouncycastle:bcpkix-jdk18on")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}