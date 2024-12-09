plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-messaging"

dependencies {
    api(project(":appserver:appserver-corelib"))
    api(project(":appserver:appserver-items"))
    api(project(":messaging:messaging-core"))
    api(project(":messaging:messaging-client"))
    api(project(":messaging:messaging-databind"))
    api("javax.ws.rs:javax.ws.rs-api")
    implementation("org.reflections:reflections")
    testFixturesImplementation("org.reflections:reflections")
    testImplementation(testFixtures(project(":seriesdata")))
    testImplementation(testFixtures(project(":appserver:appserver-corelib")))
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson")
}

coverage {
    // This is temporarily disabled to avoid unnecessarily gumming up
    // refactor work during to CRAB-23440.
}