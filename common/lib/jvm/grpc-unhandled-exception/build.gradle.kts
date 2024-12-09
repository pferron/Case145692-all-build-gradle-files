plugins {
    id("com.seeq.build.kotlin-module")
    com.seeq.build.`protobuf-grpc`
}

dependencies {
    api(platform(project(":seeq-platform")))

    api("io.grpc:grpc-kotlin-stub")
    api("io.grpc:grpc-protobuf")
    implementation("javax.inject:javax.inject")

    implementation("org.slf4j:slf4j-api")
    implementation("io.github.microutils:kotlin-logging")
}