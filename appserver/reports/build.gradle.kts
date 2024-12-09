plugins {
    com.seeq.build.appserver.`appserver-module`
}

description = "appserver-reports"

dependencies {
    implementation(project(":appserver:appserver-corelib"))
    implementation(project(":appserver:appserver-items"))
    implementation(project(":appserver:appserver-datalayer"))
    implementation("org.apache.poi:poi-ooxml")
    testImplementation("jakarta.xml.bind:jakarta.xml.bind-api")
}

coverage {
    threshold.set(0.77)
}