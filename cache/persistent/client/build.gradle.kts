plugins {
    com.seeq.build.cache.`cache-module`
}

dependencies {
    api(project(":cache:cache-persistent-debug-contract"))
    api(project(":cache:cache-persistent-contract"))
    api(project(":cache:cache-quantity-interfaces"))
    api(project(":seriesdata"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api(project(":seeq-utilities"))?.because("Needs OperationCancelledException")
    api(project(":seeq:common-grpc-load-balancing"))?.because("RoundRobinChannelPool")

    runtimeOnly("io.grpc:grpc-netty-shaded")

    testImplementation("junit:junit")
}