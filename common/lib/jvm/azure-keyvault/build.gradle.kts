group = "com.seeq.azure-keyvault"
description = "common-azure-keyvault-utility"

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.sdk.`connector-sdk-part`
}

dependencies {
    implementation("com.azure:azure-identity:1.11.4")
    implementation("com.azure:azure-security-keyvault-secrets:4.8.0")
}

tasks {
    sourcesJar {
        // Maven Central requires a source JAR, but we don't want to publish any sources for this library
        exclude("**/*")
    }
}