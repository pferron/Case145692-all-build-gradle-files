group = "com.seeq"
description = "compute-serialization-onnx"

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.`protobuf-grpc`
    com.seeq.build.protobuf
}

dependencies {
    api(platform(project(":seeq-platform")))
    api("io.grpc:grpc-kotlin-stub")
    api("io.grpc:grpc-protobuf")
}