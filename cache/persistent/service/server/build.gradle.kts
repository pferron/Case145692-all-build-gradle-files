plugins {
    com.seeq.build.cache.`cache-module`
}

dependencies {
    api(project(":cache:cache-persistent-service-core"))
    api(project(":cache:cache-persistent-contract"))
    api("com.google.inject:guice")

    implementation(project(":seeq:common-grpc-health"))
    implementation(project(":seeq:common-grpc-unhandled-exception"))
    implementation(project(":seeq:common-tiny-config-reporter"))
    implementation("com.github.jasync-sql:jasync-postgresql")
    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")
    implementation("io.grpc:grpc-netty-shaded")
}