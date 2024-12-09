group = "com.seeq.guicetohk2"

plugins {
    com.seeq.build.`kotlin-module`
}

dependencies {
    api(platform(project(":seeq-platform")))

    api("org.glassfish.jersey.inject:jersey-hk2")
    api("com.google.inject:guice")
    api("javax.ws.rs:javax.ws.rs-api")
}