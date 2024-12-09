plugins {
    com.seeq.build.appserver.`appserver-module`
    com.seeq.build.performance.jmh
    com.seeq.build.protobuf
    kotlin("plugin.serialization")
}

description = "appserver-items"

dependencies {
    api(project(":seriesdata"))
    api(project(":appserver:appserver-corelib"))

    implementation(project(":cache:cache-quantity-content"))
    implementation(project(":cache:cache-quantity-scalar"))
    implementation(project(":cache:cache-persistent-client"))
    implementation("com.opencsv:opencsv")
    implementation("com.google.guava:guava")
    implementation("org.slf4j:slf4j-api")
    implementation("com.cronutils:cron-utils")
    implementation("it.burning:cron-expression-descriptor")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

    // Mainly used by appserver-jobs, but also by items like DateRange e.g. for validation of cron-expressions.
    implementation("org.quartz-scheduler:quartz:2.3.2") {
        exclude(group = "com.zaxxer", module = "HikariCP-java7") // We already use regular HikariCP
    }
    implementation("org.quartz-scheduler:quartz-jobs:2.3.2")

    implementation("org.jsoup:jsoup") {
        because("FormulaDoc classes clean HTML tags")
    }

    implementation(project(":datasource-proxy:datasource-proxy-interfaces"))?.because(
        "AuthenticationResult",
    )
    implementation(project(":datasource-proxy:datasource-proxy-client"))?.because(
        "Datasource needs ConnectionClient",
    )

    // Generate factories for injection from Item constructors.
    compileOnlyApi(project(":seeq:common-autofactory-ksp"))
    ksp(project(":seeq:common-autofactory-ksp"))

    testImplementation("org.reflections:reflections")
    testImplementation(testFixtures(project(":appserver:appserver-corelib")))

    testFixturesApi(testFixtures(project(":seriesdata")))
    testFixturesApi("com.github.ben-manes.caffeine:caffeine")

    implementation("org.mockito:mockito-junit-jupiter")
}

coverage {
    threshold.set(0.67)
}

// TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:-this-escape",
        ),
    )
}
