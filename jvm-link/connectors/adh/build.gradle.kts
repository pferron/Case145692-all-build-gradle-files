plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-adh"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("commons-beanutils:commons-beanutils:1.9.4")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
}

coverage {
    threshold.set(0.35)
}

// TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:-this-escape",
        ),
    )
}