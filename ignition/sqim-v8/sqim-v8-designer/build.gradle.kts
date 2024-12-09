plugins {
    com.seeq.build.`java-module`
    com.seeq.build.lombok
    com.seeq.build.ignition.`ignition-base`
}

version = "${project.properties["ignitionModuleBaseVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

dependencies {
    implementation(project(":ignition:sqim-v8:sqim-v8-common"))
    implementation(project(":ignition:sqim-v8:sqim-v8-client"))
    implementation(platform(project(":ignition:sqim-v8:sqim-v8-platform")))
    compileOnly("com.inductiveautomation.ignitionsdk:ignition-common")
    compileOnly("com.inductiveautomation.ignitionsdk:client-api")
    compileOnly("com.inductiveautomation.ignitionsdk:designer-api")
    compileOnly("com.inductiveautomation.ignitionsdk:vision-designer-api")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}