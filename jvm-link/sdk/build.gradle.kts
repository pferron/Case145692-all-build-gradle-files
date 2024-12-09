plugins {
    com.seeq.build.link.jvm.`link-module`
    com.seeq.build.`kotlin-module` // To enable Kotlin tests only. Production Kotlin is disabled.
    com.seeq.build.protobuf
    com.seeq.build.sdk.`connector-sdk-part`
}

description = "seeq-link-sdk"

// The JVM Link SDK version (which is also the JVM Agent's version) has the format
//     {major}.{minor}.{patchMajor}.{patchMinor}{suffix}
//     |_____________||________________________||______|
//            |                    |               |
//         Manually       Generated from the     Set by
//        defined in     current timestamp in  the build
//     `variables.ini`      `variables.py`       flags
//
// See `crab/variables.ini` for guidance on updating the major and minor version numbers.
version = "${project.properties["linkSdkMajorVersion"]}.${project.properties["linkSdkMinorVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

/*
 * Except for slf4j, None of these dependencies should be here, the SDK should be a couple of interfaces and all
 * other logic should live inside the agent. This will take some time to untangle. Please don't add any new ones.
 */
dependencies {
    /*
     * This is the only intentional dependency of the SDK, because logging is so pervasive in all kinds of libraries,
     * and we want central control of it. slf4j is very stable, so this is not a big burden for us. We need to stay on
     * top of updates though, since connectors might have dependencies that use the latest slf4j features.
     */
    api("org.slf4j:slf4j-api")

    /*
     * Hard to remove, since the fact we use Jackson is effectively public API - Many of our own connectors use
     * customization via @JsonIgnore and similar annotations. So it stands to reason that 3rd party connectors have
     * done similar things. We should probably replace this with our own annotations in the "configuration" package
     * and then configure jackson from that.
     */
    api("com.fasterxml.jackson.core:jackson-annotations")

    /*
     * Leaked via SeeqApiProvider - We know 3rd party connectors use this, so we'll need to provide a replacement API
     * that doesn't leak all of the transitive implementation details. Then we'll need a long deprecation period
     * before we can remove this.
     */
    api(project(":seeq-sdk"))
    /*
     * Leaked via AddOnCalculationDatasourceConnection. High priority for removal, since protobuf is commonly used in
     * different versions and the leak is only in external calculation connectors, which are probably just us. At
     * least judging by the fact that we've broken that interface in binary incompatible ways recently
     * (adding getTrainedModel without a default implementation)
     */
    api("com.google.protobuf:protobuf-java")

    /*
     * Leaked via AgentLogger, leaking Kotlin transitively. High priority for removal, we don't want to leak Kotlin.
     * Also, only added recently, so unlikely to be used by 3rd parties. This API is also of questionable value to
     * 3rd parties, since it's so Seeq-focussed. Should have been an internal utility class only used by our
     * connectors instead. If we ever want this to be public API, it should be an interface provided by the
     * ConnectorService, not a collection of static methods.
     */
    api(project(":seeq:common-message"))
    /*
     * Leaked via DatasourceConnection/BaseDatasourceConnection.  High priority for removal, connectors should no
     * longer implement these outdated classes. ConnectorV2 was introduced in 2017
     */
    api(project(":seeq-utilities"))

    /*
     * The code that depends on these implementation deps is purely internal and should be in the agent instead.
     */
    implementation(project(":seeq:common-json"))
    implementation(project(":seeq:common-message"))

    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.github.tony19:named-regexp")
    implementation("com.vladsch.flexmark:flexmark:0.18.9")
    implementation("com.vladsch.flexmark:flexmark-ext-toc:0.18.9")

    // Connector Test Framework
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.junit.jupiter:junit-jupiter-params")

    // Should also remove these, since they may clash with connector implementations.
    testFixturesApi(testFixtures(project(":seeq:common-message")))

    testImplementation("org.awaitility:awaitility")
}

sourceSets {
    main {
        proto {
            srcDir("../../common/proto")
        }
    }
}

coverage {
    threshold.set(0.07)
}

tasks {
    // Production Kotlin is disabled in this project because we want to make sure all the production code is
    // written in Java to ensure near 100% parity with C#. However, in some cases, Kotlin tests are more convenient
    // or allow code reuse with other JVM projects, so we enable Kotlin tests only.
    compileKotlin {
        enabled = false
    }

    // Copy the Connector Test Framework standard test specifications from 'common'
    processTestFixturesResources {
        from("../../common/link/StandardTestData") {
            into("TestData")
        }
    }
}