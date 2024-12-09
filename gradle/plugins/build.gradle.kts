plugins {
    idea
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    val kotlinVersion = "1.9.21"
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
    implementation("io.swagger.codegen.v3:swagger-codegen:3.0.30")
    implementation("io.swagger.codegen.v3:swagger-codegen-generators:1.0.30")
    implementation("io.swagger.core.v3:swagger-jaxrs2:2.1.12")
    implementation("io.swagger:swagger-jaxrs:1.5.16")
    implementation("javax.xml.bind:jaxb-api:2.3.0")
    implementation("javax.ws.rs:javax.ws.rs-api:2.1")
    implementation("javax.servlet:javax.servlet-api:3.1.0")
    implementation("org.reflections:reflections:0.9.12")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.21-1.0.15")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:4.2.0")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.0")
    implementation("nu.studer:gradle-jooq-plugin:9.0")
    implementation("com.gradle:gradle-enterprise-gradle-plugin:3.13")
    implementation("com.github.spotbugs:spotbugs:4.8.3")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.influxdb:influxdb-client-java:6.3.0")
    implementation("de.thetaphi:forbiddenapis:3.7")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("org.yaml:snakeyaml:1.31")
    implementation("com.expediagroup:graphql-kotlin-gradle-plugin:6.4.0")
    implementation("org.revapi:revapi-java:0.28.1")
    implementation("org.revapi:revapi-basic-features:0.13.0")
    implementation("org.revapi:revapi-reporter-json:0.5.0")
    implementation("com.jayway.jsonpath:json-path:2.9.0")
    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.1.8")

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

idea.project.jdkName = "Java ${JavaVersion.current().majorVersion}"