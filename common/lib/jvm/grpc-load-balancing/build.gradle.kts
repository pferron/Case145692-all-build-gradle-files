plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api("io.grpc:grpc-api")

    implementation("com.google.guava:guava")
    implementation("org.slf4j:slf4j-api")
    implementation("io.github.microutils:kotlin-logging")
}