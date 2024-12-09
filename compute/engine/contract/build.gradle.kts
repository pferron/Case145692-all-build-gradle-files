plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.`protobuf-grpc`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":seriesdata"))
    api(project(":seeq:common-activity"))
    api(project(":seeq:common-monitoring")) { because("TimerHistogram") }
    api(project(":seeq:common-data-coordinates"))
    api("io.grpc:grpc-kotlin-stub")
    api("io.grpc:grpc-protobuf")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("javax.annotation:javax.annotation-api")

    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation(project(":compute:data"))

    testFixturesApi(project(":appserver:appserver-compute"))
    testFixturesApi("junit:junit")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi(kotlin("test-junit"))
    testFixturesApi(testFixtures(project(":seriesdata")))
}