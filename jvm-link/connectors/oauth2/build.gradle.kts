plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-oauth2"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.nimbusds:oauth2-oidc-sdk:10.1")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.2")
    /** nimbusds libraries use this bouncycastle dependency as an optional dependency,
     * so its version must be compatible with nimbusds's specified versions
     * https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support
     */
    implementation("org.bouncycastle:bcpkix-jdk18on")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.4")
}

coverage {
    threshold.set(0.20)
}