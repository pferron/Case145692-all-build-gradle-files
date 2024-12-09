plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-jobs"

dependencies {
    implementation(project(":appserver:appserver-corelib"))
    implementation(project(":appserver:appserver-compute"))
    implementation(project(":appserver:appserver-items"))
    implementation(project(":appserver:appserver-tables"))
    implementation(project(":appserver:appserver-messaging"))
    implementation(project(":appserver:appserver-drivers-graph"))
    implementation(project(":seeq:common-concurrent"))
    implementation(project(":cache:cache-quantity-content"))
    implementation(project(":datasource-proxy:datasource-proxy-client"))?.because("ConnectionCommandRouter")

    implementation("dev.failsafe:failsafe")
    implementation("com.google.inject.extensions:guice-servlet")
    implementation("commons-validator:commons-validator")
    implementation("it.burning:cron-expression-descriptor")

    // For a Jersey injection annotation that determines which scheduler to use
    implementation("org.glassfish.hk2:hk2-api:2.5.0-b42")
    // Transitive dependency of quartz and quartz-jobs. Prior version (9.5.2) has security issue (CRAB-14883)
    implementation("com.mchange:c3p0:0.9.5.4")
    api("org.quartz-scheduler:quartz:2.3.2") {
        exclude(group = "com.mchange", module = "c3p0")
        exclude(group = "com.zaxxer", module = "HikariCP-java7") // We already use regular HikariCP
    }
    api("org.quartz-scheduler:quartz-jobs:2.3.2") {
        exclude(group = "com.mchange", module = "c3p0")
    }
    implementation("org.glassfish.jersey.core:jersey-client")
    implementation("org.apache.httpcomponents:httpcore:4.4.13")

    testImplementation(testFixtures(project(":appserver:appserver-corelib")))
    testImplementation(testFixtures(project(":appserver:appserver-compute")))
    testImplementation(testFixtures(project(":appserver:appserver-tables")))
    testImplementation(testFixtures(project(":appserver:appserver-messaging")))
    testImplementation(testFixtures(project(":appserver:appserver-server")))
    testImplementation(testFixtures(project(":appserver:appserver-drivers-graph")))
    testImplementation(project(":cache:cache-persistent-client")) { because("PersistentCacheDeletionJobIT") }
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
}

coverage {
    threshold.set(0.58)
    excludes.addAll("**/proto/**")
}