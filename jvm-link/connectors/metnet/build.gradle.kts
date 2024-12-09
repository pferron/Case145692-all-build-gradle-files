plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-metnet"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("org.apache.commons:commons-collections4")
}

coverage {
    threshold.set(0.58)
}

tasks {
    externalITs {
        /**
         * The Metnet testing infrastructure is hosted in their network, so we can only run the external integration
         * tests manually. Use the `manualTest` task for that.
         */
        enabled = false
    }
}