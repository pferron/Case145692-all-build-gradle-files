plugins {
    com.seeq.build.cache.`cache-module`
    com.seeq.build.`protobuf-grpc`
}

dependencies {
    api(project(":seeq:common-monitoring"))
    api("io.grpc:grpc-kotlin-stub")
    api("io.grpc:grpc-protobuf")
}