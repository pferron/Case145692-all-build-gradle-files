plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.`protobuf-grpc`
}

dependencies {
    api(platform(project(":seeq-platform")))
    implementation("io.grpc:grpc-stub")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}