plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.`protobuf-grpc`
}

dependencies {
    api(platform(project(":seeq-platform")))
    implementation("io.grpc:grpc-stub")

    testImplementation("ch.qos.logback:logback-classic")
    testImplementation("org.slf4j:slf4j-api")
    testImplementation("io.github.microutils:kotlin-logging")

    testImplementation("org.assertj:assertj-core")
    testImplementation("io.grpc:grpc-kotlin-stub")
    testImplementation("io.grpc:grpc-protobuf")
    testImplementation("io.grpc:grpc-testing")
    testImplementation("io.grpc:grpc-netty-shaded")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testImplementation("com.google.guava:guava") {
        because("SettableFuture")
    }
}

coverage {
    // Code was 100% covered before adding `inline` but Jacoco does not account for coverage of inline methods
    // See https://github.com/jacoco/jacoco/issues/654
    threshold.set(0.3)
}