plugins {
    com.seeq.build.`java-module`
    com.seeq.build.lombok
    com.seeq.build.ignition.`ignition-base`
}

version = "${project.properties["ignitionModuleBaseVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

dependencies {
    compileOnly("com.inductiveautomation.ignitionsdk:ignition-common")
    implementation(platform(project(":ignition:sqim-v8:sqim-v8-platform")))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}