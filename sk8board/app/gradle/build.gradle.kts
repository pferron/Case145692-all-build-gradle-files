import org.gradle.configurationcache.extensions.serviceOf
import com.seeq.build.isWindows
import com.seeq.build.isLinux
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import PyTest
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
    com.seeq.build.helm.chart
}


abstract class CreateVenv @Inject constructor(private val exec: ExecOperations) : DefaultTask() {
    // This is referenced through devRequirements, so it must be included as an input too.
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val requirements: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val venvDir: DirectoryProperty

    @get:OutputFile
    val pythonExecutable: Provider<String> by lazy {
        this.venvDir.map {
            if (isWindows) "${it.asFile}/Scripts/python.exe" else "${it.asFile}/bin/python"
        }
    }

    @TaskAction
    fun createVirtualEnv() {
        venvDir.get().asFile.deleteRecursively()
        // UV defaults to hardlinking on non Apple operating systems:
        // https://github.com/astral-sh/uv/blob/ad8e3a2c/crates/install-wheel-rs/src/linker.rs#L251-L259
        // However, this is a problem on Windows because hardlinks create confusion because one hardlink
        // being locked prevents the others from being deleted. Linux doesn't have that issue.
        var linkMode = if (isLinux) "hardlink" else "clone"
        exec.exec {
            commandLine("python", "-m", "uv",
                        "venv", "--quiet", "--link-mode", linkMode, "--seed", venvDir.get().asFile)
        }
        exec.exec {
            commandLine(
                pythonExecutable.get(), "-E", "-m", "pip", "install", "uv", "--quiet",
            )
        }
        exec.exec {
            commandLine(
                pythonExecutable.get(), "-E", "-m", "uv", "pip", "install", "--quiet",
                *requirements.files.flatMap { listOf("-r", it.path) }.toTypedArray(),
            )
        }
    }
}


// The gradle plugin for IntelliJ doesn't support specifying the project type or a python SDK,
// so the gradle stuff must be contained in a subdirectory to prevent breaking intellisense
// https://github.com/JetBrains/gradle-idea-ext-plugin/issues/134
val baseDir = projectDir.parent

tasks {

    val createVirtualEnv by registering(CreateVenv::class) {
        requirements.from(
            file("$baseDir/requirements.txt"),
            file("$baseDir/requirements.dev.txt"),
        )
        venvDir.set(file("$baseDir/.venv"))
    }

    rootProject.idea.project {
        settings {
            taskTriggers {
                afterSync(createVirtualEnv)
            }
        }
    }

    dockerBuildContext {
        from("$baseDir/requirements.txt")
        from("$baseDir/server") {
            exclude("**/test_*.py")
            exclude("**/test_*.pem")
            exclude("**/test_*.json")
            exclude("**/pytest.ini")
            into("server")
        }
        from("$baseDir/client") {
            exclude("**/*.test.ts")
            exclude("**/*.test.tsx")
            exclude("**/*.stories.tsx")
            exclude("**/node_modules/**")
            into("client")
        }
    }

    dockerBuild {
        imageName.set("sk8board-app")
    }

    register("generateXrd") {
        dependsOn(":sk8board:app:client:assemble")

        val exec = serviceOf<ExecOperations>()
        val cli = "$baseDir/cli.py"
        val wDir = "${rootProject.projectDir}"
        val xrd = "${rootProject.projectDir}/sk8board/seeq-crossplane-xrds/helm-chart/templates/tenant-xrd.yaml"
        val pythonExecutable = createVirtualEnv.flatMap { it.pythonExecutable }

        inputs.files(cli, xrd, pythonExecutable)
        inputs.files("$baseDir/schema_generator.py")
        outputs.dir("$baseDir/server/generated")
        outputs.dir("$baseDir/client/src/types/generated")
        doLast {
            exec.exec {
                executable(pythonExecutable.get())
                args(cli, "generate-xrd", "--file", xrd)
                environment["PYTHONPATH"] = "$wDir"
            }
        }
    }

    val pyTest by registering(PyTest::class) {
        inputs.dir(createVirtualEnv.flatMap { it.venvDir })
        pythonExecutable.set(createVirtualEnv.flatMap { it.pythonExecutable })
        disableWarnings.set(true)
        reportFile.set(file("$baseDir/target/reports/pytest/test.xml"))
        with(sources) {
            from(baseDir)
            include("server/**")
            exclude("**/__pycache__/**")
        }
    }

    check {
        dependsOn(pyTest)
    }
}
