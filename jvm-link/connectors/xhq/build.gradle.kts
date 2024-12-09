plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-xhq"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

repositories {
    flatDir {
        dir("lib")
    }
}

dependencies {
    implementation(":xhqcoreapi:[4.7,7.0]")
    implementation(":xhqsolapi:[4.7,7.0]")
    implementation(":xhqmodelapi:[4.7,7.0]")
    implementation(":xhqpmapi:[4.7,7.0]")
}

coverage {
    threshold.set(0.87)
}

tasks {
    externalITs {
        /**
         * The XHQ testing infrastructure is hosted in their network, so we can only run the external integration
         * tests manually. Use the `manualTest` task for that.
         */
        enabled = false
    }
}