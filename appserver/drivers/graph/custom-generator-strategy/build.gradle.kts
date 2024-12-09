plugins {
    id("com.seeq.build.kotlin-module")
    com.seeq.build.appserver.`appserver-module`
}

description = "Custom generator strategy for jOOQ"

dependencies {
    implementation("org.jooq:jooq-codegen")
}