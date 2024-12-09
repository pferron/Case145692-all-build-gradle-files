import java.time.Year

plugins {
    com.seeq.build.base
}

val devLicense by configurations.creating
val devLicenseFile = file("$rootDir/sq-run-data-dir/licenses/Development.lic")

val installDevLicense by tasks.registering(Exec::class) {
    val outputFile = devLicenseFile
    inputs.files(
        fileTree("build") {
            include("**/*.py")
        },
    ).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(
        fileTree("$rootDir/common/squish") {
            include("**/*.py")
        },
    ).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.property("yearOfValidity", Year.now().value)
    outputs.file(outputFile)
    outputs.cacheIf { true }
    workingDir(projectDir)
    commandLine("python", "-m", "build", "install")

    doLast {
        if (!outputFile.exists()) {
            throw GradleException("Failed to generate a developer license; is there an error in the output?")
        }
    }
}

tasks.assemble {
    dependsOn(installDevLicense)
}

artifacts {
    add(devLicense.name, devLicenseFile) {
        builtBy(installDevLicense)
    }
}