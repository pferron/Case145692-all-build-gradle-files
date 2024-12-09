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
    compileOnly("com.inductiveautomation.ignitionsdk:client-api")
    compileOnly("com.inductiveautomation.ignitionsdk:designer-api")
    compileOnly("com.inductiveautomation.ignitionsdk:vision-client-api")
    compileOnly("com.inductiveautomation.ignitionsdk:vision-designer-api")
    implementation(platform(project(":ignition:sqim-v7:sqim-v7-platform")))
    implementation(project(":ignition:sqim-v7:sqim-v7-common"))
}

java {
    // TODO CRAB-42075: Remove when Ignition 7.9 support is deprecated
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            // ignition 7 module requires JDK8 source compatibility, which now throws a warning
            "-Xlint:-options",
            // new-to-JDK21 warning from calling an overrideable method on `this` in a constructor.
            // See https://www.oracle.com/java/technologies/javase/21all-relnotes.html#JDK-8015831
            "-Xlint:-this-escape",
        ),
    )
}