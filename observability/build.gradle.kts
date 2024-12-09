plugins {
    com.seeq.build.docker.docker
}

val otelAgent by configurations.creating
val pyroscopeExtension by configurations.creating

dependencies {
    otelAgent(platform(project(":seeq-platform")))
    otelAgent("io.opentelemetry.javaagent:opentelemetry-javaagent")

    pyroscopeExtension(platform(project(":seeq-platform")))
    pyroscopeExtension(project(":seeq:common-pyroscope", "shadow"))
}

tasks {
    dockerBuildContext {
        from(otelAgent) {
            rename { "opentelemetry-javaagent.jar" }
        }
        from(pyroscopeExtension) {
            rename { "seeq-opentelemetry-extension.jar" }
        }
    }
    val image by registering(Sync::class) {
        from(otelAgent) {
            rename { "opentelemetry-javaagent.jar" }
        }
        from(pyroscopeExtension) {
            rename { "pyroscope-extension.jar" }
        }
        into("image")
    }
}