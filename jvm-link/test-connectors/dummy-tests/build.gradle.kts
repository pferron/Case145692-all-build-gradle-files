plugins {
    com.seeq.build.link.jvm.`link-module`
}

group = "com.dummycorp.tests"
description = "dummycorp-tests"

buildDir = file("target")

dependencies {
    implementation(project(":jvm-link:seeq-link-sdk"))
    implementation(project(":jvm-link:seeq-link-agent"))
    testCompileOnly(project(":jvm-link:dummy-connector-atlanta"))
    testCompileOnly(project(":jvm-link:dummy-connector-boston"))
}

tasks {
    withType<Test>().configureEach {
        // CRAB-28325: DependencyIsolationIT failing on reflective access.
        // Remove as part of refactor to avoid breakage when removed
        jvmArgs("--add-opens", "java.base/java.net=ALL-UNNAMED")
    }
}