plugins {
    com.seeq.build.`kotlin-module`
}

group = "com.seeq"
description = "seriesdata"

val jamm by configurations.creating
configurations {
    testImplementation {
        extendsFrom(jamm)
    }
}

dependencies {
    api(platform(project(":seeq-platform")))
    api(project(":seeq:common-activity"))
    api(project(":seeq:common-message"))
    api("javax.inject:javax.inject")
    api("com.seeq.jscience:jscience")
    api("commons-io:commons-io")
    api("commons-cli:commons-cli")
    api("commons-configuration:commons-configuration")
    api("com.google.guava:guava")
    api(group = "com.googlecode.json-simple", name = "json-simple", version = "1.1.1") {
        exclude(group = "junit", module = "junit")
    }
    api("org.apache.httpcomponents:httpclient")
    api("org.apache.httpcomponents:httpmime")
    api("com.google.code.gson:gson")
    api("org.slf4j:slf4j-api")
    api("javax.annotation:javax.annotation-api")
    api("io.github.microutils:kotlin-logging")
    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("io.dropwizard.metrics:metrics-core")

    implementation("org.apache.commons:commons-lang3")
    implementation(project(":seeq-utilities"))
    implementation(project(":seeq:common-message"))
    implementation("com.github.ben-manes.caffeine:caffeine")

    testFixturesApi("junit:junit")
    testFixturesApi("org.mockito:mockito-core")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi(kotlin("test-junit"))
    testFixturesApi("org.mockito.kotlin:mockito-kotlin")

    testImplementation(testFixtures(project(":seeq-test-utilities")))
    testImplementation(testFixtures(project(":seeq:common-message")))
    testImplementation("com.google.guava:guava-testlib")
    testImplementation(testFixtures(project(":jvm-link:seeq-link-sdk")))
        ?.because("Sharing SplitDatumsBase test with jvm-link")

    jamm("com.github.jbellis:jamm:0.3.3")
}

coverage {
    threshold.set(0.70) // CRAB-38860 to raise this to 0.73+ again
}

tasks {
    withType<Test>().configureEach {
        jvmArgumentProviders.add(JammAgent(jamm))
    }
}

data class JammAgent(@get:InputFiles @get:Classpath val jammPath: FileCollection) : CommandLineArgumentProvider {
    override fun asArguments() = listOf("-javaagent:${jammPath.asPath}")
}