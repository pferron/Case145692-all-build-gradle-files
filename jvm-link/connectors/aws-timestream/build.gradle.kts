plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-aws-timestream"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation(project(":jvm-link:seeq-link-connector-commons-tabular"))
    implementation("software.amazon.awssdk:timestreamquery:2.15.54")
    testImplementation("org.awaitility:awaitility")
}

tasks {
    externalITs {
        /**
         * The AWS-Timestream testing infrastructure is hosted in their network, so we can only run the external
         * integration tests manually. Use the `manualTest` task for that.
         */
        enabled = false
    }
}