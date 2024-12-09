plugins {
    id("com.seeq.build.ignition.ignition-module")
}

version = "${project.properties["ignitionModuleBaseVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

val ignitionVersion: String by project

ignitionModule {
    moduleId.set("com.seeq.ignition")
    moduleName.set("Seeq")
    moduleDescription.set(
        "Trending, visualizations, calculations, analytics, cleansing, modeling, advanced  monitoring and reporting.",
    )
    moduleVersion.set(version.toString().removeSuffix(project.properties["seeqVersionSuffix"]?.toString() ?: ""))
    requiredIgnitionVersion.set(ignitionVersion)
    requiredFrameworkVersion.set("8")
    scopes {
        named("gateway") {
            includeProject("sqim-v8-gateway", "shadow")
            hook("com.seeq.ignition.v8.gateway.GatewayHook")
        }
        named("client") {
            includeProject("sqim-v8-client")
        }
        named("designer") {
            includeProject("sqim-v8-client")
            includeProject("sqim-v8-designer")
            hook("com.seeq.ignition.designer.DesignerHook")
        }
    }
}