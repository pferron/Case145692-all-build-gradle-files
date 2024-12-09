plugins {
    com.seeq.build.`java-module`
    com.github.johnrengelman.shadow
}

dependencies {
    api(platform(project(":seeq-platform")))
    compileOnly("io.opentelemetry:opentelemetry-api")
    compileOnly("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api")
    compileOnly("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api")
    compileOnly("org.postgresql:postgresql")
    implementation("io.pyroscope:agent")
}

java {
    // We use this to profile older Seeq versions, so keep this as backwards compatible as possible
    sourceCompatibility = JavaVersion.VERSION_11
}