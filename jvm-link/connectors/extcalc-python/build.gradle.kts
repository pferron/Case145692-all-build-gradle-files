plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-extcalc-python"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("org.apache.commons:commons-collections4")
    testImplementation("org.awaitility:awaitility")
}

tasks {
    val copyScripts by registering(Copy::class) {
        from("src/main/python-scripts")
        into("$buildDir/python-scripts")
        include("**/*.py")
        include("**/*.txt")
    }

    jar {
        dependsOn(copyScripts)
    }

    test {
        inputs.files(
            fileTree("src/main/python-scripts") {
                include("**/*.py")
                include("**/*.txt")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        inputs.files(
            fileTree("src/test/python-scripts") {
                include("**/*.py")
                include("**/*.txt")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    }

    image {
        from(copyScripts) {
            into("python-scripts")
        }
    }
}