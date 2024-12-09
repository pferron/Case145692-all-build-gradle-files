plugins {
    com.seeq.build.datasourceproxy.`datasource-proxy-module`
}

dependencies {
    api(project(":datasource-proxy:datasource-proxy-contract"))
    api(project(":datasource-proxy:datasource-proxy-interfaces"))?.because("Types used in client method signatures")
    api(project(":seeq:common-data-coordinates"))

    implementation(project(":seeq-utilities"))
    implementation(project(":seeq:common-grpc-load-balancing"))?.because("RoundRobinChannelPool")
    implementation(project(":seeq:common-monitoring"))
    implementation(project(":seeq:common-seeq-monitors"))
    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation(project(":seeq:common-grpc-client-utils"))

    implementation("com.google.inject:guice")

    runtimeOnly("io.grpc:grpc-netty-shaded")

    testApi(project(":datasource-proxy:datasource-proxy-contract"))

    testImplementation(project(":seeq-utilities"))

    testImplementation("com.google.inject:guice")
    testImplementation("io.grpc:grpc-testing")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation(testFixtures(project(":seriesdata")))
}

tasks {
    compileTestKotlin {
        kotlinOptions.freeCompilerArgs =
            kotlinOptions.freeCompilerArgs + listOf("-opt-in=kotlin.io.path.ExperimentalPathApi")
    }
}