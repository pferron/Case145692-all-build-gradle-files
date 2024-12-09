import com.seeq.build.performance.Jmh

plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.performance.jmh
}

description = "System tests of backend components, including appserver. " +
    "Originally these were integration tests but grew to include other components, " +
    "and now run against a full Seeq product, making them system tests. " +
    "We strongly prefer to write contract tests for individual components due to reliability, runtime, " +
    "and ease of debugging; prefer contract tests over system tests whenever possible."

group = "com.seeq"

dependencies {
    testFixturesApi("org.junit.platform:junit-platform-launcher")
    testFixturesApi("org.apache.olingo:olingo-odata2-api:2.0.11")
    testFixturesApi("org.apache.olingo:odata-client-api:4.10.0")
    testFixturesApi("org.apache.olingo:odata-client-core:4.10.0")
    testFixturesApi(testFixtures(project(":cache:cache-pipeline-series")))
    testFixturesApi(testFixtures(project(":cache:cache-persistent-client")))
    testFixturesApi(testFixtures(project(":appserver:appserver-server")))
    testFixturesApi(testFixtures(project(":appserver:appserver-compute")))
    testFixturesApi(
        testFixtures(project(":compute:compute-engine-debug-contract")) {
            because("MemoryCacheClearer")
        },
    )
    testFixturesApi(testFixtures(project(":datasource-proxy:datasource-proxy-client")))
    testFixturesApi(testFixtures(project(":jvm-link:seeq-link-agent")))
    testFixturesApi(testFixtures(project(":jvm-link:seeq-link-connector-example-data")))
    testFixturesApi(testFixtures(project(":jvm-link:seeq-link-connector-chaos-monkey")))
    testFixturesApi(testFixtures(project(":jvm-link:seeq-link-connector-stress-test")))

    testImplementation("javax.annotation:javax.annotation-api")
    testImplementation("jakarta.xml.bind:jakarta.xml.bind-api")
    testImplementation("org.glassfish.jaxb:jaxb-runtime")
    testImplementation("ch.qos.logback:logback-classic")
    testImplementation("org.slf4j:slf4j-api")
    testImplementation("org.slf4j:jul-to-slf4j")
    testImplementation("io.github.microutils:kotlin-logging")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testImplementation("com.github.jbellis:jamm")
    testImplementation("com.google.code.findbugs:annotations")
    testImplementation("org.awaitility:awaitility")
    testImplementation(project(":seeq-sdk"))
    testImplementation(project(":seeq-utilities"))
    testImplementation(testFixtures(project(":seeq-test-utilities")))
    testImplementation(project(":appserver:appserver-analytics"))
    testImplementation(project(":appserver:appserver-drivers-signal-file"))
    testImplementation(project(":appserver:appserver-jobs"))
    testImplementation(project(":appserver:appserver-messaging"))

    testImplementation(project(":compute:compute-engine-service"))
    testImplementation(project(":compute:compute-formula-support-service"))
    testImplementation(project(":datasource-proxy:datasource-proxy-contract"))
    testImplementation(project(":datasource-proxy:datasource-proxy-core"))
    testImplementation(project(":datasource-proxy:datasource-proxy-interfaces"))
    testImplementation(project(":datasource-proxy:datasource-proxy-service"))

    testRuntimeOnly(project(":messaging:messaging-service-server"))
    testRuntimeOnly(files("$rootDir/product/nginx-conf"))

    testImplementation("org.jboss.resteasy:resteasy-client")
    testImplementation("org.jboss.resteasy:resteasy-multipart-provider:3.5.1.Final")
    testImplementation("com.jayway.jsonpath:json-path")
    testImplementation("org.glassfish.tyrus.bundles:tyrus-standalone-client-jdk")
    testImplementation("org.glassfish.tyrus:tyrus-container-grizzly-server")
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("com.github.tomakehurst:wiremock-jre8")
    testImplementation("org.postgresql:postgresql")
    testImplementation(project(":appserver:appserver-drivers-graph"))
    testImplementation("com.github.romankh3:image-comparison:4.4.0")
    testImplementation("org.apache.poi:poi-ooxml")
    testImplementation("org.reflections:reflections")
    testImplementation("org.glassfish.grizzly:grizzly-websockets")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
}

tasks {
    withType<Test>().configureEach {
        inputs.files("../product/image").withNormalizer(ClasspathNormalizer::class)
        doLast {
            // Throw away the previous test data so Gradle always runs the tests in the same order instead of failed
            // first. This is unfortunately necessary because many of the tests in this project are order sensitive.
            binaryResultsDirectory.get().asFile.deleteRecursively()
        }
    }

    test {
        enabled = false
    }

    slowTests {
        enabled = false
    }

    register("systemTest", Test::class) {
        group = "verification"
        description = "Run system tests. " +
            "These run the full product, so you need to run sq build -f && sq image -n before running them."

        (this.options as JUnitPlatformOptions).excludeTags(
            "com.seeq.common.testcategories.SlowTest",
            "com.seeq.common.testcategories.FuzzTest",
        )

        configure<JacocoTaskExtension> {
            isEnabled = false
        }
    }

    register("slowSystemTest", Test::class) {
        group = "verification"
        description = "Run slow system tests. " +
            "These run the full product, so you need to run sq build -f && sq image -n before running them."

        (this.options as JUnitPlatformOptions).includeTags("com.seeq.common.testcategories.SlowTest")
        (this.options as JUnitPlatformOptions).excludeTags("com.seeq.common.testcategories.FuzzTest")

        configure<JacocoTaskExtension> {
            isEnabled = false
        }
    }

    microBenchmarks {
        enabled = false
    }

    macroBenchmarks {
        enabled = false
    }

    register<Jmh>("systemBenchmarks") {
        inputs.files("../product/image").withNormalizer(ClasspathNormalizer::class)
        includes.add(".*SystemBenchmark.*")
        forks.set(0)
        iterations.set(3)
        iterationTime.set("5s")
        warmupIterations.set(3)
        warmupIterationTime.set("5s")
        benchmarkMode.set("avgt")
    }

    register<Jmh>("materializedTableBenchmarks") {
        inputs.files("../product/image").withNormalizer(ClasspathNormalizer::class)
        includes.add(".*MaterializedTableBenchmark.*")
        forks.set(0)
        iterations.set(5)
        warmupIterations.set(1)
        benchmarkMode.set("avgt")
    }
}