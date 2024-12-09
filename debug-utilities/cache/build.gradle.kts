plugins {
    id("com.seeq.build.kotlin-module")
    id("com.seeq.build.application")
}

description = "cache debug utilities"

dependencies {
    implementation(platform(project(":seeq-platform")))
    implementation(project(":compute:compute-engine-debug-contract"))
    implementation(project(":seeq:common-serialization-seriesdata"))
    implementation(project(":compute:data"))
    implementation("io.grpc:grpc-netty-shaded")
    implementation("org.jetbrains.kotlinx:kotlinx-cli")
}

application {
    mainClass.set("com.seeq.debugutilities.cache.MainKt")
}