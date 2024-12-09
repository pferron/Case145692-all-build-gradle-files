plugins {
    com.seeq.build.`java-module`
    com.seeq.build.lombok
    com.seeq.build.ignition.`ignition-base`
    com.github.johnrengelman.shadow
}

version = "${project.properties["ignitionModuleBaseVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

dependencies {
    compileOnly("com.inductiveautomation.ignitionsdk:ignition-common")
    compileOnly("com.inductiveautomation.ignitionsdk:gateway-api")
    compileOnly("com.inductiveautomation.ignitionsdk:driver-api")

    shadow(project(":ignition:sqim-v7:sqim-v7-common"))
    implementation(platform(project(":ignition:sqim-v7:sqim-v7-platform")))
    implementation(project(":jvm-link:seeq-link-agent"))
    implementation(project(":jvm-link:seeq-link-sdk"))

    testImplementation("junit:junit")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("com.inductiveautomation.ignitionsdk:ignition-common")
    testImplementation("com.inductiveautomation.ignitionsdk:gateway-api")
    testImplementation("com.inductiveautomation.ignitionsdk:driver-api")
    testImplementation(project(":ignition:sqim-v7:sqim-v7-common"))
}

java {
    // TODO CRAB-42075: Remove when Ignition 7.9 support is deprecated
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    shadowJar {
        relocate("org.eclipse.jetty", "shaded.seeq.org.eclipse.jetty")
        relocate("com.google.gson", "shaded.seeq.com.google.gson")
        relocate("com.google.protobuf", "shaded.seeq.com.google.protobuf") // CRAB-40723
        relocate("kotlin", "shaded.seeq.kotlin")
        transform(com.seeq.build.shadow.KotlinResourcesRelocator("shaded.seeq.kotlin"))
        mergeServiceFiles()

        manifest {
            attributes(
                // Since Ignition uses a fat jar containing all dependencies we need to differentiate the versions
                // of the agent and connector.
                "Agent-Version" to project(":jvm-link:seeq-link-agent").version,
                // The Ignition module includes the expected SDK version
                "Minimum-Seeq-Link-SDK-Version" to project(":jvm-link:seeq-link-sdk").version,
            )
        }
    }

    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                // ignition 7 module requires JDK8 source compatibility, which now throws a warning
                "-Xlint:-options",
            ),
        )
    }
}