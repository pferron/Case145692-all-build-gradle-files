plugins {
    com.seeq.build.datasourceproxy.`datasource-proxy-module`
    com.seeq.build.`protobuf-grpc`
}

dependencies {
    api(project(":seeq:common-serialization-seriesdata"))
    api(project(":seriesdata"))

    implementation(project(":seeq-utilities"))

    api("io.grpc:grpc-kotlin-stub")
    api("io.grpc:grpc-protobuf")

    testImplementation("junit:junit")
}