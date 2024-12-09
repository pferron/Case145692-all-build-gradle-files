plugins {
    com.seeq.build.cache.`cache-module`
}

dependencies {
    api(project(":seeq:common-tiny-config"))
    api("javax.inject:javax.inject")
    implementation("com.github.jasync-sql:jasync-postgresql")

    testImplementation(testFixtures(project(":cache:cache-persistent-testfixtures")))
    testImplementation("io.kotest:kotest-assertions-core-jvm")
}