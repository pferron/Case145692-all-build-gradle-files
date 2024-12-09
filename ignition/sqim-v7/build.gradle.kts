plugins {
    id("com.seeq.build.ignition.ignition-module")
}

version = "${project.properties["ignitionModuleBaseVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

val ignitionVersion: String by project
val seeqVersionNoSuffix: String by project.extra

ignitionModule {
    moduleId.set("com.seeq.ignition")
    moduleName.set("Seeq")
    moduleDescription.set(
        "Trending, visualizations, calculations, analytics, cleansing, modeling, advanced  monitoring and reporting.",
    )
    moduleVersion.set(seeqVersionNoSuffix)
    requiredIgnitionVersion.set(ignitionVersion)
    requiredFrameworkVersion.set("7")
    scopes {
        named("gateway") {
            includeProject("sqim-v7-gateway", "shadow")
            hook("com.seeq.ignition.v7.gateway.GatewayHook")
        }
        named("client") {
            includeProject("sqim-v7-client")
        }
        named("designer") {
            includeProject("sqim-v7-client")
            includeProject("sqim-v7-designer")
            hook("com.seeq.ignition.designer.DesignerHook")
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    executable("${System.getenv("JAVA8_HOME")}/bin/java")
}