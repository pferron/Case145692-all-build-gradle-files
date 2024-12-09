/*
 * This project contains the platform for the CRAB project's dependencies. These are dependencies in use by the final
 * product (i.e. CRAB, JVM-Link, but not the individual connectors) extracted here so that we can align versions across
 * the product and resolve transitive dependencies. For more information about platforms, see
 * https://docs.gradle.org/current/userguide/java_platform_plugin.html#sec:java_platform_separation
 */

import org.jetbrains.kotlin.config.KotlinCompilerVersion

group = "com.seeq"

plugins {
    com.seeq.build.base
    `java-platform`
    com.seeq.build.sdk.`connector-sdk-part`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    val dropwizardMetricsVersion = "3.2.6"
    val protobufVersion = "3.25.5"
    val grpcKotlinVersion = "1.4.0"
    val jooqVersion = "3.19.4"
    val swaggerCodegenVersion = "3.0.29"
    val swaggerCodegenGeneratorsVersion = "1.0.29"
    val swaggerAnnotationsVersion = "2.1.11"
    val openTelemetryVersion = "1.29.0"
    val graphqlKotlinVersion = "6.5.2"

    api(platform("org.junit:junit-bom:5.7.0"))
    api(platform("com.fasterxml.jackson:jackson-bom:2.13.4.20221013"))
    api(platform("org.glassfish.jersey:jersey-bom:2.40"))
    api(platform("org.glassfish.grizzly:grizzly-bom:2.4.4"))
    api(platform("io.grpc:grpc-bom:1.58.0"))

    constraints {
        api("com.seeq.jscience:jscience:5.2.7")
        api("ch.qos.logback:logback-classic:1.3.14") {
            because("CRAB-39703: Once we upgrade from Java EE to Jakarta EE, we should be able to update this version to 1.4.x instead")
        }
        api("net.logstash.logback:logstash-logback-encoder:7.4")
        api("com.mchange.c3p0:0.9.5.5") {
            because("CRAB-14883")
        }
        api("com.github.ben-manes.caffeine:caffeine:3.1.1")
        api("com.github.jasync-sql:jasync-postgresql:1.1.6")
        api("com.github.jbellis:jamm:0.3.2")
        api("com.github.tony19:named-regexp:0.2.4")
        api("com.google.auto.factory:auto-factory:1.0-beta8")
        api("com.google.code.findbugs:annotations:3.0.1")
        api("com.google.code.gson:gson:2.10")
        api("com.google.guava:guava:32.1.2-jre")
        api("com.google.inject:guice:6.0.0")
        api("com.google.protobuf:protoc:$protobufVersion")
        api("com.google.protobuf:protobuf-java:$protobufVersion")
        api("com.google.protobuf:protobuf-kotlin:$protobufVersion")
        api("com.google.inject.extensions:guice-servlet:4.2.3")
        api("com.opencsv:opencsv:5.7.1")
        api("com.opentable.components:otj-pg-embedded:1.0.3-SEEQ")
        api("com.cronutils:cron-utils:9.2.1")
        api("it.burning:cron-expression-descriptor:1.2.6")
        api("commons-configuration:commons-configuration:1.10")
        api("commons-net:commons-net:3.6")
        api("commons-cli:commons-cli:1.4")
        api("commons-io:commons-io:2.17.0")
        api("dev.failsafe:failsafe:3.1.0")
        api("io.dropwizard.metrics:metrics-core:$dropwizardMetricsVersion")
        api("io.dropwizard.metrics:metrics-servlets:$dropwizardMetricsVersion")
        api("io.github.microutils:kotlin-logging:1.7.10")
        api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
        api("io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion")
        api("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:$openTelemetryVersion")
        api("io.opentelemetry.javaagent:opentelemetry-javaagent:$openTelemetryVersion")
        api("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:$openTelemetryVersion-alpha")
        api("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.2.0")
        api("io.opentelemetry:opentelemetry-api:$openTelemetryVersion")
        api("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$openTelemetryVersion-alpha")
        api("io.pyroscope:agent:0.13.0")
        api("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
        api("javax.annotation:javax.annotation-api:1.3.1")
        api("javax.inject:javax.inject:1")
        api("javax.servlet:javax.servlet-api:4.0.1")
        api("net.openhft:zero-allocation-hashing:0.12")
        api("org.apache.commons:commons-collections4:4.4")
        api("org.apache.commons:commons-text:1.10.0")
        api("org.apache.commons:commons-lang3:3.12.0")
        api("org.apache.commons.compress:1.26") {
            because("Lower versions got flagged by Whitesource")
        }
        api("org.apache.httpcomponents:httpclient:4.5.13")
        api("org.apache.httpcomponents:httpmime:4.5.13")
        api("org.apache.poi:poi-ooxml:5.2.3")
        api("org.apache.xmlgraphics:batik-all:1.17") {
            because("CRAB-38848 used by poi-ooxml, 1.16 and below have CVE-2022-44729")
        }
        api("org.glassfish.jaxb:jaxb-runtime:2.3.2")

        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
        api("org.reflections:reflections:0.9.12")
        api("org.jsoup:jsoup:1.18.1")
        api("org.postgresql:postgresql:42.7.2")
        api("org.slf4j:jul-to-slf4j:2.0.6")
        api("org.slf4j:slf4j-api:2.0.6")
        api("uk.org.lidalia:sysout-over-slf4j:1.0.2")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
        api("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
        api(kotlin("reflect", version = KotlinCompilerVersion.VERSION))
        api(kotlin("stdlib-jdk8", version = KotlinCompilerVersion.VERSION))

        api("io.swagger.codegen.v3:swagger-codegen:$swaggerCodegenVersion")
        api("io.swagger.codegen.v3:swagger-codegen-generators:$swaggerCodegenGeneratorsVersion")
        api("io.swagger.core.v3:swagger-annotations:$swaggerAnnotationsVersion")
        api("com.brsanthu:migbase64:2.2")
        api("com.github.ajalt.clikt:clikt:3.0.1") // library for parsing command line input in kt
        api("org.apache.servicemix.bundles:org.apache.servicemix.bundles.dom4j:2.1.3_1") {
            because(
                "the version that Apache Directory API uses (2.1.1_1) contains a security vulnerability. " +
                    "See CRAB-22577 for more details.",
            )
        }
        api("`org.apache.logging.log4j:log4j:2.17.1") {
            because(
                "versions below 2.15.0 contain the serious vulnerabilities CVE-2021-44228, CVE-2021-45046 " +
                    "and CVE-2021-45105.  See CRAB-28307 for more details. As of 2022-01-03, Log4J is not used by " +
                    "Seeq or any dependency of Seeq. This entry is a defensive approach such that if it ever gets " +
                    "introduced inadvertently, it will at least be an updated version that does not contain the " +
                    "aforementioned vulnerabilities.",
            )
        }
        api("org.apache.zookeeper:zookeeper:3.6.1") {
            because("Lower versions got flagged by Whitesource")
        }
        api("org.apache.hadoop:hadoop-client:2.9.2") {
            because("Lower versions got flagged by Whitesource")
        }
        api("com.sun.jersey:jersey-core:1.13") {
            because("Lower versions got flagged by Whitesource (CRAB-20215)")
        }
        api("com.sun.jersey:jersey-server:1.13") {
            because("Lower versions got flagged by Whitesource")
        }
        api("javax.ws.rs:javax.ws.rs-api:2.1.1")
        api("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
        api("org.bouncycastle:bcpkix-jdk18on:1.79")
        api("org.jooq:jooq-meta:$jooqVersion")
        api("org.jooq:jooq-codegen:$jooqVersion")
        api("org.jooq:jooq:$jooqVersion")

        api("com.expediagroup:graphql-kotlin-server:$graphqlKotlinVersion")
        api("com.expediagroup:graphql-kotlin-hooks-provider:$graphqlKotlinVersion")
        api("com.expediagroup:graphql-kotlin-client:$graphqlKotlinVersion")

        api("org.eclipse.jetty.websocket:websocket-client:9.4.53.v20231009") {
            because("it has a dependency on jetty-http which causes CVE-2023-36478 (CRAB-39652)")
        }
        api("commons-validator:commons-validator:1.7")

        // Test dependencies
        api("commons-fileupload:1.5") {
            because("CRAB-36316 Required by wiremock, 1.4 and below causes CVE-2023-24998")
        }
        api("org.eclipse.jetty:jetty-http:9.4.53.v20231009") {
            because("CRAB-39652 Required by wiremock, 9.4.52.v20230823 and below causes CVE-2023-36478")
        }
        // TODO: CRAB-42075 replace with the JRE 11+ compatible com.github.tomakehurst:wiremock when Ignition 7.9
        //  support is deprecated
        api("com.github.tomakehurst:wiremock-jre8:2.35.1")

        api("com.google.guava:guava-testlib:30.1-jre")
        api("com.jayway.jsonpath:json-path:2.9.0")
        api("net.minidev:json-smart:2.4.10") {
            because("CRAB-36463 json-smart:2.3 causes CVE-2023-1370")
        }
        api("org.mockito.kotlin:mockito-kotlin:4.1.0")
        api("junit:junit:4.13")
        api("org.assertj:assertj-core") {
            version {
                strictly("3.19.0")
                because("https://youtrack.jetbrains.com/issue/KT-52942 and https://youtrack.jetbrains.com/issue/KT-53113")
            }
        }
        api("org.glassfish.tyrus.bundles:tyrus-standalone-client-jdk:1.13.1")
        api("org.glassfish.tyrus:tyrus-container-grizzly-server:1.13.1")
        api("org.jboss.resteasy:resteasy-client:3.5.1.Final") // CRAB-6871, more reliable than jersey for ITs
        api("org.mockito:mockito-core:5.8.0")
        api("org.mockito:mockito-junit-jupiter:5.9.0")
        api(kotlin("test-junit", version = KotlinCompilerVersion.VERSION))
        api("io.kotest:kotest-assertions-core-jvm:5.5.1")
        api("org.awaitility:awaitility:4.2.2")

        api("io.netty:netty-handler:4.1.101.Final") {
            because(
                "CRAB-39485: versions lower than 4.1.101 contain the vulnerability CVE-2023-4586. All dependent " +
                    "packages other than org.apache.olingo:odata-server-core:4.10.0 reference the safe 4.1.101. This " +
                    "forces odata-server-core to use a safe version instead of 4.1.74 as it does now",
            )
        }
        api("io.netty:netty-handler-proxy:4.1.101.Final") {
            because(
                "CRAB-39485: versions lower than 4.1.101 contain the vulnerability CVE-2023-4586. All dependent " +
                    "packages other than com.microsoft.azure.kusto:kusto-data:4.0.0 reference the safe 4.1.101. This " +
                    "forces kusto-data to use a safe version instead of 4.1.78 as it does now",
            )
        }
        api("com.nimbusds:nimbus-jose-jwt:9.37.2") {
            because(
                "CRAB-42102: versions lower than 9.37.2 contain the vulnerability CVE-2023-52428. A few packages " +
                    "(e.g azure-identity, micronaut-runtime) have this as a transitive dependency and reference " +
                    "9.30.2 or 9.37.1 both of which have the vulnerability",
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}