group = "com.seeq.configuration"

plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api("javax.inject:javax.inject")
    api("com.google.inject:guice")

    implementation(project(":seeq:common-tiny-config"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.assertj:assertj-core")
}

// TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:-this-escape",
        ),
    )
}