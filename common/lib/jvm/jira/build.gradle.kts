group = "com.seeq.jira"

plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))
    api("org.glassfish.jersey.core:jersey-client")
    api("org.glassfish.jersey.inject:jersey-hk2")
    api("org.glassfish.jersey.media:jersey-media-json-jackson")
    api("org.glassfish.jersey.media:jersey-media-multipart")
    api("org.glassfish.jaxb:jaxb-runtime")
    implementation("javax.ws.rs:javax.ws.rs-api")

    implementation(project(":seeq-utilities"))

    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation(kotlin("test-junit"))
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("com.github.tomakehurst:wiremock-jre8")
}