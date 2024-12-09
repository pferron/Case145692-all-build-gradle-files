group = "com.seeq.supervisor"
description = "supervisor-core"

plugins {
    com.seeq.build.application
    `maven-publish`
    com.seeq.build.`kotlin-module`
}

application {
    mainClass.set("com.seeq.supervisor.core.Main")
}

dependencies {
    implementation(platform(project(":seeq-platform")))
    implementation(project(":seeq-utilities"))
    implementation(project(":seeq-sdk"))
    implementation("commons-cli:commons-cli")
    implementation("commons-configuration:commons-configuration")
    implementation("org.glassfish.grizzly:grizzly-http-server")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.apache.httpcomponents:httpmime")
    implementation("commons-net:commons-net")
    implementation("com.google.code.gson:gson")
    implementation("javax.annotation:javax.annotation-api")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("org.glassfish.jaxb:jaxb-runtime")
    implementation("ch.qos.logback:logback-classic")
    implementation("org.slf4j:slf4j-api")
    implementation("org.slf4j:jul-to-slf4j")
    implementation("io.github.microutils:kotlin-logging")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.github.jbellis:jamm")
    implementation("com.google.code.findbugs:annotations")

    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("com.github.tomakehurst:wiremock-jre8")
}

coverage {
    threshold.set(0.29)
}

tasks {
    register<Copy>("copyConfigs") {
        from("$projectDir/src/main/configuration")
        into("$buildDir/configuration")
        include("*.xml")
    }

    register<Copy>("copyResources") {
        from("$projectDir/src/main/resources")
        into("$buildDir/configuration")
        include("*.xml")
    }

    jar {
        dependsOn("copyConfigs", "copyResources")
    }
}