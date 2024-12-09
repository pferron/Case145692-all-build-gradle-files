import com.seeq.build.isWindows
import org.gradle.internal.os.OperatingSystem

plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
    com.seeq.build.helm.chart
}

// The gradle plugin for IntelliJ doesn't support specifying the project type or a python SDK,
// so the gradle stuff must be contained in a subdirectory to prevent breaking intellisense
// https://github.com/JetBrains/gradle-idea-ext-plugin/issues/134
val baseDir = projectDir.parent

tasks {
    dockerBuildContext {
        from("$baseDir/pyproject.toml")
        from("$baseDir/poetry.lock")
        from("$baseDir/app") {
            into("app")
        }
    }

    dockerBuild {
        buildArgs.put("POETRY_VERSION", pythonPackageVersion("poetry"))
    }

    val createPoetryLockFile by registering(Exec::class) {
        inputs.files(file("$baseDir/pyproject.toml")).withPathSensitivity(PathSensitivity.RELATIVE)
            .ignoreEmptyDirectories()
        outputs.file(file("$baseDir/poetry.lock"))

        workingDir = file(baseDir)
        commandLine = listOf("sh", "-c", "poetry lock --no-update")
    }

    val createVirtualEnvWithPoetry by registering(Exec::class) {
        inputs.files(file("$baseDir/poetry.lock")).withPathSensitivity(PathSensitivity.RELATIVE)
            .ignoreEmptyDirectories()
        inputs.files(file("$baseDir/pyproject.toml")).withPathSensitivity(PathSensitivity.RELATIVE)
            .ignoreEmptyDirectories()
        inputs.property("operatingSystem", OperatingSystem.current().familyName)
        outputs.dir(File(baseDir, ".venv"))
        outputs.cacheIf { true }

        workingDir = file(baseDir)
        commandLine = listOf(
            "sh",
            "-c",
            "poetry config virtualenvs.in-project true && poetry install --no-interaction --no-ansi --no-root",
        )

        mustRunAfter(createPoetryLockFile)
    }

    val testUnit by registering(PyTest::class) {
        testConfiguration(this, "unit")
        dependsOn(createVirtualEnvWithPoetry)
    }

    val testIntegration by registering(PyTest::class) {
        testConfiguration(this, "integration")
        dependsOn(createVirtualEnvWithPoetry)
    }

    val testPerformance by registering(PyTest::class) {
        testConfiguration(this, "performance")
        dependsOn(createVirtualEnvWithPoetry)
    }

    check {
        dependsOn(testUnit)
        dependsOn(testIntegration)
    }
}

fun testConfiguration(task: PyTest, testType: String) {
    val testFileTree = fileTree(baseDir) {
        include(".coveragec")
        include("pytest.ini")
        include("app/**")
        include("tests/**")
        exclude("**/__pycache__/**")
    }
    val report = layout.buildDirectory.file("test-reports/test-$testType.xml")
    val pythonExecutable = if (isWindows) "$baseDir/.venv/Scripts/python.exe" else "$baseDir/.venv/bin/python"
    val extraArgs = when (testType) {
        "unit" -> "--cov ./ --cov-report html --cov-config ./.coveragec"
        "integration" -> ""
        "performance" -> ""
        else -> throw GradleException("Invalid test type: $testType")
    }

    task.group = "verification"
    task.inputs.property("operatingSystem", OperatingSystem.current().familyName)
    task.inputs.files(testFileTree).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()

    with(task.sources) {
        from(file(baseDir))
        exclude("**")
    }
    task.mark.set(testType)
    task.disableWarnings.set(true)
    task.reportFile.set(report)
    task.pythonExecutable.set(pythonExecutable)
    task.extraArgs.set(extraArgs)

    task.outputs.dir(layout.buildDirectory.file("coverage"))
    task.outputs.file(".coverage")
    task.outputs.file(report)
    task.outputs.cacheIf { true }
}

fun pythonPackageVersion(pythonPackage: String) =
    providers.exec {
        commandLine("pip", "list")
    }.standardOutput.asText.map { output ->
        output.lines().single { """^${Regex.escape(pythonPackage)}\s+\S+$""".toRegex().matches(it) }
            .removePrefix(pythonPackage).trim()
    }