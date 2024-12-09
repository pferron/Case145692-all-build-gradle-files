plugins {
    id("com.seeq.build.kotlin-module")
    com.seeq.build.`protobuf-grpc`
}

dependencies {
    api(platform(project(":seeq-platform")))

    api("io.grpc:grpc-kotlin-stub")
    api("io.grpc:grpc-protobuf")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("javax.annotation:javax.annotation-api")

    implementation("org.slf4j:slf4j-api")
    implementation("io.github.microutils:kotlin-logging")
}