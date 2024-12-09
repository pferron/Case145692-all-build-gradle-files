plugins {
    com.seeq.build.cache.`cache-module`
}

dependencies {
    testFixturesApi("com.github.jasync-sql:jasync-postgresql")
    testFixturesApi("com.opentable.components:otj-pg-embedded")
    testFixturesImplementation("org.assertj:assertj-core")

    testFixturesApi(project(":cache:cache-persistent-service-server"))
    testFixturesApi(project(":cache:cache-quantity-interfaces"))
}