plugins {
    id("com.seeq.build.kotlin-module")
}

dependencies {
    api(platform(project(":seeq-platform")))
    api("com.google.auto.factory:auto-factory:1.0.1")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")
}