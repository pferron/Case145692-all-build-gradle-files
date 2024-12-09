plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-adx"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation(project(":jvm-link:seeq-link-connector-commons-tabular"))
    implementation(project(":seeq:common-azure-keyvault"))
    implementation("com.microsoft.azure.kusto:kusto-data:4.0.4")
}

coverage {
    threshold.set(0.40)
}