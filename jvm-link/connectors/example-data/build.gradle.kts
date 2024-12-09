plugins {
    com.seeq.build.link.jvm.connector
    com.seeq.build.performance.jmh
}

description = "seeq-link-connector-example-data"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("ch.randelshofer:fastdoubleparser:0.9.0")
    implementation("com.ethlo.time:itu:1.7.3")
    implementation("org.glassfish.jaxb:jaxb-runtime")
}

coverage {
    threshold.set(0.74)
}

tasks {
    val copyData by registering(Copy::class) {
        from(file("$projectDir/src/main/data"))
        into(file("$buildDir/data"))
    }

    jar {
        dependsOn(copyData)
        manifest {
            attributes("Seeq-Link-Isolated-ClassLoader" to true)
        }
    }

    image {
        from(copyData) {
            into("data")
        }
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