plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.lombok
    com.seeq.build.sdk.`connector-sdk-part`
}

group = "com.seeq.utilities"
description = "seeq-utilities"

val devLicense by configurations.creating
val seeqNames by configurations.creating

dependencies {
    api(platform(project(":seeq-platform")))
    api("commons-io:commons-io")
    api("commons-cli:commons-cli")
    api("commons-configuration:commons-configuration")
    api("com.google.guava:guava")
    api(group = "com.googlecode.json-simple", name = "json-simple", version = "1.1.1") {
        exclude(group = "junit", module = "junit")
    }
    api("org.apache.httpcomponents:httpclient")
    api("org.apache.httpcomponents:httpmime")
    api("com.google.code.gson:gson")
    api("org.slf4j:slf4j-api")
    api("io.dropwizard.metrics:metrics-core")
    api("com.fasterxml.jackson.module:jackson-module-parameter-names")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("jakarta.xml.bind:jakarta.xml.bind-api")
    api("ch.qos.logback:logback-classic")
    api("org.slf4j:jul-to-slf4j")
    api("io.github.microutils:kotlin-logging")
    api("com.github.jbellis:jamm")
    api("com.google.code.findbugs:annotations")

    implementation(
        group = "com.googlecode.owasp-java-html-sanitizer", name = "owasp-java-html-sanitizer",
        version = "20211018.2",
    )

    testImplementation(testFixtures(project(":seeq-test-utilities")))
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("com.github.tomakehurst:wiremock-jre8")

    devLicense(project(":licensing", "devLicense"))
}

coverage {
    threshold.set(0.54)
}

val seeqNamesFile = File(buildDir, "SeeqNames.json")
val generateSeeqnamesJson by tasks.registering(JavaExec::class) {
    outputs.file(seeqNamesFile)
    mainClass.set("com.seeq.utilities.SeeqNamesGenerator")
    classpath(sourceSets["main"].runtimeClasspath)
}

artifacts {
    add(seeqNames.name, generateSeeqnamesJson.map { seeqNamesFile })
}

tasks {
    val writeBuildProperties by creating {
        val seeqVersion = providers.gradleProperty("seeqVersion")
        val seeqMarketingVersion = providers.gradleProperty("seeqMarketingVersion")
        val revision = providers.exec {
            commandLine("git", "--no-optional-locks", "rev-parse", "--verify", "HEAD")
        }.standardOutput.asText
        inputs.property("seeqVersion", seeqVersion)
        inputs.property("seeqMarketingVersion", seeqMarketingVersion)
        inputs.property("revision", revision)
        val outputFile = file("$buildDir/build.properties")
        outputs.file(outputFile)
        outputs.cacheIf { true }
        doLast {
            outputFile.writeText(
                """
                build.version=${seeqVersion.get()}
                build.version.marketing=${seeqMarketingVersion.get()}
                build.commit=${revision.get()}
                """.trimIndent(),
            )
        }
    }
    processResources {
        from(writeBuildProperties)
    }
    assemble {
        dependsOn(generateSeeqnamesJson)
    }
    withType<Test>().configureEach {
        inputs.files(
            rootProject.fileTree("common/configuration") {
                include("**/*.py")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        dependsOn(devLicense)
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