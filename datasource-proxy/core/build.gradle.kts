plugins {
    com.seeq.build.datasourceproxy.`datasource-proxy-module`
    com.seeq.build.protobuf
}

dependencies {
    api("javax.servlet:javax.servlet-api")
    api("jakarta.ws.rs:jakarta.ws.rs-api")

    api(project(":datasource-proxy:datasource-proxy-interfaces"))

    implementation(project(":seeq:common-data-coordinates"))
    implementation(project(":seeq:common-serialization-seriesdata"))

    implementation("javax.inject:javax.inject")
    implementation(project(":seeq-utilities"))

    api(project(":seriesdata"))
    implementation(project(":seeq:common-concurrent"))
    implementation("org.glassfish.grizzly:grizzly-websockets")
    implementation("org.glassfish.grizzly:grizzly-http-servlet")
    implementation("com.google.protobuf:protobuf-java")
    implementation("com.github.ben-manes.caffeine:caffeine")

    testImplementation(testFixtures(project(":seeq:common-monitoring")))
    testImplementation("org.apache.commons:commons-lang3")
    testImplementation("com.google.inject:guice")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation(testFixtures(project(":seriesdata")))
}

sourceSets {
    main {
        proto {
            srcDir("../../common/proto")
        }
    }
}

coverage {
    threshold.set(0.70)
    excludes.addAll("**/link/messages/**/*.class")
}

tasks {
    compileTestKotlin {
        kotlinOptions.freeCompilerArgs =
            kotlinOptions.freeCompilerArgs + listOf("-opt-in=kotlin.io.path.ExperimentalPathApi")
    }

    // TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:-this-escape",
            ),
        )
    }
}