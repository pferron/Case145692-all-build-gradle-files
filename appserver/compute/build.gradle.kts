plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-compute"

dependencies {
    api(project(":seriesdata"))
    api(project(":appserver:appserver-corelib"))?.because("SimpleCapsule")
    api(project(":appserver:appserver-items"))
    api(project(":compute:compute-engine-contract"))
    api(project(":compute:compute-formula-support-contract"))

    api("io.opentelemetry:opentelemetry-api")

    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation(project(":seeq:common-grpc-load-balancing"))
    implementation(project(":seeq:common-grpc-streaming-utils"))
    implementation(project(":seeq:common-open-telemetry"))

    testImplementation("io.grpc:grpc-testing")
    testFixturesApi(testFixtures(project(":appserver:appserver-query")))
        ?.because("Faking serializer behavior for item fetching in DataDescriptionFactoryTest.")
}