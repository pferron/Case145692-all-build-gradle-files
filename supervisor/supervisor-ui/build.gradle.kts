plugins {
    com.seeq.build.application
    `maven-publish`
}

group = "com.seeq.supervisor"
description = "supervisor-ui"

dependencies {
    implementation(platform(project(":seeq-platform")))
    implementation(project(":seeq-utilities"))
    implementation(project(":seeq-sdk"))
    implementation(project(":seeq:common-jira"))
    implementation("commons-cli:commons-cli")
    implementation("commons-configuration:commons-configuration")
    implementation("org.glassfish.grizzly:grizzly-http-server")
    implementation(group = "com.googlecode.json-simple", name = "json-simple", version = "1.1.1") {
        exclude(group = "junit", module = "junit")
    }
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.apache.httpcomponents:httpmime")
    implementation("commons-net:commons-net")
    implementation("com.miglayout:miglayout:3.7.4")
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

application {
    mainClass.set("com.seeq.supervisor.ui.Main")
}

tasks {
    register<Copy>("copyResources") {
        from("$projectDir/src/main/resources")
        into("$buildDir/configuration")
        include("*.xml")
    }

    jar {
        dependsOn("copyResources")
    }

    // TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:-this-escape",
            ),
        )
    }
}