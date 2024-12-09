/**
 * Provides dependency constraints to ensure that our projects will compile and run against the right library
 * versions for this Ignition version.
 */
plugins {
    `java-platform`
}

version = "${project.properties["ignitionModuleBaseVersion"]}" +
    ".${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"

dependencies {
    constraints {
        val ignitionVersion: String by project

        api("com.inductiveautomation.ignitionsdk:ignition-common:$ignitionVersion")
        api("com.inductiveautomation.ignitionsdk:client-api:$ignitionVersion")
        api("com.inductiveautomation.ignitionsdk:designer-api:$ignitionVersion")
        api("com.inductiveautomation.ignitionsdk:vision-client-api:$ignitionVersion")
        api("com.inductiveautomation.ignitionsdk:vision-designer-api:$ignitionVersion")
        api("com.inductiveautomation.ignitionsdk:gateway-api:$ignitionVersion")
        api("com.inductiveautomation.ignitionsdk:driver-api:$ignitionVersion")
        api("ch.qos.logback:logback-classic") {
            version { strictly("1.1.7") }
        }
        api("org.eclipse.milo:sdk-client") {
            version { strictly("0.1.0") }
        }
    }
}