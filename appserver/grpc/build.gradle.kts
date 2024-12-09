plugins {
    com.seeq.build.appserver.`appserver-module`
    com.seeq.build.`protobuf-grpc`
    com.seeq.build.performance.jmh
}

description = "appserver-grpc"
val seeqVersion: String by project

dependencies {
    val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
    implementation(project(":datasource-proxy:datasource-proxy-contract"))

    implementation(project(":seriesdata"))
    implementation(project(":appserver:appserver-items"))
    implementation(project(":appserver:appserver-drivers-condition"))
    implementation(project(":appserver:appserver-drivers-signal-file"))

    implementation(project(":seeq:common-monitoring"))
    implementation(project(":seeq:common-seeq-monitors"))
    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation(project(":seeq:common-grpc-health"))
    implementation(project(":seeq:common-grpc-unhandled-exception"))

    implementation("io.grpc:grpc-netty-shaded")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    testImplementation(testFixtures(project(":appserver:appserver-server")))
    testImplementation(project(":datasource-proxy:datasource-proxy-client"))
    testImplementation("io.grpc:grpc-testing")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation(project(":compute:compute-formula-support-contract"))

    jmhImplementation(testFixtures(project(":appserver:appserver-server")))
    jmhImplementation(project(":appserver:appserver-drivers-graph"))
    jmhImplementation(project(":datasource-proxy:datasource-proxy-client"))
    jmhImplementation("io.grpc:grpc-testing")
    jmhImplementation("io.kotest:kotest-assertions-core-jvm")
    jmhImplementation(project(":compute:compute-formula-support-contract"))
}

coverage {
    threshold.set(0.7)
    excludes.addAll("**/com/seeq/pipeline_configuration/*")
}