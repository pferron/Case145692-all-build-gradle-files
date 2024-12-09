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
    implementation(project(":ignition:sqim-v8:sqim-v8-common"))
    implementation(platform(project(":ignition:sqim-v8:sqim-v8-platform")))
    implementation("com.google.code.gson:gson:2.8.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            // new-to-JDK21 warning from calling an overrideable method on `this` in a constructor.
            // See https://www.oracle.com/java/technologies/javase/21all-relnotes.html#JDK-8015831
            "-Xlint:-this-escape",
        ),
    )
}