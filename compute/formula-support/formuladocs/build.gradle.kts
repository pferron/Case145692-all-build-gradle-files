plugins {
    com.seeq.build.`kotlin-module`
}
group = "com.seeq"
description = "compute-formula-support-formuladocs"

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":seriesdata"))
    implementation("org.reflections:reflections")
    implementation("commons-io:commons-io")
    implementation("io.github.classgraph:classgraph:4.8.177")
    implementation("org.commonmark:commonmark-parent:0.22.0")
    implementation("org.commonmark:commonmark:0.22.0")
    testImplementation(project(":appserver:appserver-analytics"))
    testImplementation(project(":appserver:appserver-compiler"))
    testFixturesApi(project(":appserver:appserver-compiler"))

    testFixturesApi("junit:junit")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi(kotlin("test-junit"))
}