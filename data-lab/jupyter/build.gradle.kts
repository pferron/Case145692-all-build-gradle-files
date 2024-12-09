import com.seeq.build.crossPlatformCommandLine
import com.seeq.build.getProperty
import com.seeq.build.isFullySpecifiedTag
import com.seeq.build.isWindows
import com.seeq.build.jupyterPackages
import com.seeq.build.mainlineTagCheck
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
}

val pyPiPackage: Configuration by configurations.creating
val pyPiPackageDist: Configuration by configurations.creating
val pyPiPackageSpyDist: Configuration by configurations.creating
val tagPackagesFile: Configuration by configurations.creating
val configFile: File = project(":data-lab").layout.projectDirectory.dir("gradle.tasks.properties").asFile
val containerRegistry: String = getProperty(configFile, "registry")
val localTagFile: String = getProperty(configFile, "image.local.tag")
val repositoryTag: String = getProperty(configFile, "image.repository.tag")
val specifiedPackagesImageName: String = getProperty(configFile, "$jupyterPackages.name")
val specifiedPackagesTag: String = getProperty(
    project(":data-lab:data-lab-jupyter-packages").layout.projectDirectory.dir("image.properties").asFile,
    "$jupyterPackages.tag",
)

dependencies {
    pyPiPackageDist(project(":seeq-sdk", "pyPiPackageDist"))
    pyPiPackageSpyDist(project(":seeq-sdk", "pyPiPackageSpyDist"))
    tagPackagesFile(project(":data-lab:data-lab-jupyter-packages", "tagPackagesFile"))
}

tasks {
    val sdlExtensionDir = file("seeq-datalab-extension")
    val sdlExtensionFileTree = fileTree(sdlExtensionDir) {
        exclude("**/__pycache__/")
        exclude("dist/")
        exclude("lib/")
        exclude("node_modules/")
        exclude("seeq_datalab_extension.egg-info/")
        exclude("seeq_datalab_extension/.coverage/")
        exclude("seeq_datalab_extension/.pytest_cache/")
        exclude("seeq_datalab_extension/coverage/")
        exclude("seeq_datalab_extension/labextension/")
        exclude("seeq-datalab-extension.iml")
        exclude("tsconfig.tsbuildinfo")
    }

    val generateSdlExtensionPackage by registering {
        inputs.files(sdlExtensionFileTree).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        outputs.dir(File(sdlExtensionDir, "dist"))
        outputs.dir(File(sdlExtensionDir, "seeq_datalab_extension.egg-info"))
        outputs.dir(File(sdlExtensionDir, "seeq_datalab_extension/labextension"))
        outputs.cacheIf { true }
        val fs = serviceOf<FileSystemOperations>()
        val exec = serviceOf<ExecOperations>()
        val logLevel = pythonLogLevel
        doFirst {
            fs.delete {
                delete(
                    "$sdlExtensionDir/dist", "$sdlExtensionDir/seeq_datalab_extension.egg-info",
                    "$sdlExtensionDir/seeq_datalab_extension/labextension",
                )
            }
            exec.exec {
                workingDir(sdlExtensionDir)
                crossPlatformCommandLine("python $logLevel -m build --wheel")
            }
        }
    }

    val testSdlExtensionPackage by registering(PyTest::class) {
        sdlTestConfiguration(
            this, "testSdlExtensionPackage", sdlExtensionFileTree,
            sdlExtensionDir.resolve("seeq_datalab_extension"), "unit",
        )
        dependsOn(generateSdlExtensionPackage)
    }

    val testSystemSdlExtensionPackage by registering(PyTest::class) {
        sdlTestConfiguration(
            this, "testSystemSdlExtensionPackage", sdlExtensionFileTree,
            sdlExtensionDir.resolve("seeq_datalab_extension"), "system",
        )
        dependsOn(generateSdlExtensionPackage)
    }

    val sdlApiDir = file("seeq-datalab-api")
    val sdlApiFileTree = fileTree(sdlApiDir) {
        exclude("**/__pycache__/")
        exclude(".coverage/")
        exclude(".logs/")
        exclude(".pytest_cache/")
        exclude("coverage/")
        exclude("dist/")
        exclude("seeq_datalab_api.egg-info/")
        exclude("seeq-datalab-api.iml")
    }

    val generateSdlApiPackage by registering {
        dependsOn(generateSdlExtensionPackage)
        inputs.property("operatingSystem", OperatingSystem.current().familyName)
        inputs.files(sdlApiFileTree).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        outputs.dir(File(sdlApiDir, "dist"))
        outputs.dir(File(sdlApiDir, "seeq_datalab_api.egg-info"))
        outputs.cacheIf { true }
        val fs = serviceOf<FileSystemOperations>()
        val exec = serviceOf<ExecOperations>()
        val logLevel = pythonLogLevel
        doFirst {
            fs.delete {
                delete("$sdlApiDir/dist", "$sdlApiDir/seeq_datalab_api.egg-info")
            }
            exec.exec {
                workingDir(sdlApiDir)
                crossPlatformCommandLine("python $logLevel -m build --wheel")
            }
        }
    }

    val testSdlApiPackage by registering(PyTest::class) {
        sdlTestConfiguration(this, "testSdlApiPackage", sdlApiFileTree, sdlApiDir, "unit")
        dependsOn(generateSdlExtensionPackage, generateSdlApiPackage)
    }

    val testSystemSdlApiPackage by registering(PyTest::class) {
        sdlTestConfiguration(this, "testSystemSdlApiPackage", sdlApiFileTree, sdlApiDir, "system")
        dependsOn(generateSdlExtensionPackage, generateSdlApiPackage)
    }

    val sdlGptExtensionDir = file("seeqgpt-datalab-extension")
    val sdlGptFileTree = fileTree(sdlGptExtensionDir) {
        exclude("**/__pycache__/")
        exclude("dist/")
        exclude("lib/")
        exclude("node_modules/")
        exclude("seeqgpt_datalab_extension.egg-info/")
        exclude("seeqgpt_datalab_extension/labextension/")
        exclude("seeqgpt-datalab-extension.iml")
        exclude("tsconfig.tsbuildinfo")
    }

    val generateSdlGptExtensionPackage by registering {
        inputs.files(sdlGptFileTree).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        outputs.dir(File(sdlGptExtensionDir, "dist"))
        outputs.dir(File(sdlGptExtensionDir, "seeqgpt_datalab_extension.egg-info"))
        outputs.dir(File(sdlGptExtensionDir, "seeqgpt_datalab_extension/labextension"))
        outputs.cacheIf { true }
        val fs = serviceOf<FileSystemOperations>()
        val exec = serviceOf<ExecOperations>()
        val logLevel = pythonLogLevel
        doFirst {
            fs.delete {
                delete(
                    "$sdlGptExtensionDir/dist", "$sdlGptExtensionDir/seeqgpt_datalab_extension.egg-info",
                    "$sdlGptExtensionDir/seeqgpt_datalab_extension/labextension",
                )
            }
            exec.exec {
                workingDir(sdlGptExtensionDir)
                crossPlatformCommandLine("python $logLevel -m build --wheel")
            }
        }
    }

    val downloadSpyFromPyPI by registering {
        val exec = serviceOf<ExecOperations>()
        val fs = serviceOf<FileSystemOperations>()
        val spyDownloadDir = "target/spy-download"
        doFirst {
            fs.delete {
                delete(spyDownloadDir)
            }
            exec.exec {
                crossPlatformCommandLine("pip download seeq-spy --no-deps --dest $spyDownloadDir")
            }
        }
        outputs.files(
            fileTree(spyDownloadDir) {
                include("*.whl")
            },
        )
    }

    dockerBuildContext {
        from(pyPiPackageDist) {
            include("*.whl")
            into("sdk_and_spy")
        }
        if (System.getenv().getOrDefault("SQ_BRANCH_IS_RELEASE", "false") == "true") {
            from(downloadSpyFromPyPI) {
                include("*.whl")
                into("sdk_and_spy")
            }
        } else {
            from(pyPiPackageSpyDist) {
                include("*.whl")
                into("sdk_and_spy")
            }
        }
        from(generateSdlExtensionPackage) {
            include("*.whl")
            into("seeq-datalab-extension")
        }
        from(generateSdlApiPackage) {
            include("*.whl")
            into("seeq-datalab-api")
        }
        from(generateSdlGptExtensionPackage) {
            include("*.whl")
            into("seeqgpt-datalab-extension")
        }
        from(projectDir) {
            include("seeq/**")
            exclude("seeq/isotree/**")
            exclude("**/*.iml")
        }
        from("$sdlExtensionDir/style") {
            include("root.css")
            include("widgets.css")
            into("seeq/voila/static/seeq")
        }
    }

    dockerBuild {
        imageName.convention("datalab-jupyter")
        imageTagFile.set(project.layout.buildDirectory.dir(localTagFile).get().asFile)
        val seeqMarketingVersion: String by project
        // data-lab server will import a tar file of this image, so it must match the naming scheme in data_lab.py
        datalabServerImage.set("seeq/datalab-jupyter")
        imageName.set("datalab-jupyter")

        mainlineTagCheck(specifiedPackagesTag)
        if (isFullySpecifiedTag(specifiedPackagesTag)) {
            buildArgs.put("BASE_IMAGE", "$containerRegistry/$specifiedPackagesImageName")
        } else {
            buildArgs.put("BASE_IMAGE", specifiedPackagesImageName)
        }

        buildArgs.put("POETRY_VERSION", pythonPackageVersion("poetry"))
        buildArgs.put("JUPYTER_VERSION", seeqMarketingVersion)
        baseImageTagFile.fileProvider(tagPackagesFile.elements.map { it.single().asFile })

        dependsOn(":data-lab:data-lab-jupyter-packages:dockerBuild")
    }

    dockerPublish {
        imageTagFile.set(project.file(project.layout.buildDirectory.dir(repositoryTag).get().asFile.absolutePath))
        dependsOn(":data-lab:data-lab-jupyter-packages:dockerPublish")
    }

    register("mergeTagFiles", MergeTagFilesTask::class) {
        group = "CiBuild"
        description = "Collects the versions of the jupyter images and writes them to a file"
        enabled = com.seeq.build.isDockerSupported
        inputTagFiles.set(
            listOf(
                project.layout.buildDirectory.dir(repositoryTag).get().asFile,
                project(":data-lab:data-lab-jupyter-packages").layout.buildDirectory.dir(repositoryTag).get().asFile,
                project(":data-lab:data-lab-jupyter-base").layout.buildDirectory.dir(repositoryTag).get().asFile,
            ),
        )
        outputFile.set(project.layout.buildDirectory.dir("docker-output/image-tags.txt").get().asFile)
        dependsOn(":data-lab:data-lab-jupyter-packages:dockerRetag")
    }

    check {
        dependsOn(testSdlApiPackage)
        dependsOn(testSdlExtensionPackage)
    }
    clean {
        dependsOn("cleanGenerateSdlExtensionPackage")
        dependsOn("cleanGenerateSdlApiPackage")
    }
}

val Task.pythonLogLevel by lazy {
    when {
        logger.isDebugEnabled -> "-v"
        logger.isInfoEnabled -> ""
        else -> "-q"
    }
}

fun pythonPackageVersion(pythonPackage: String): Provider<String> =
    providers.exec {
        commandLine("pip", "list")
    }.standardOutput.asText.map { output ->
        output.lines()
            .single { """^${Regex.escape(pythonPackage)}\s+\S+$""".toRegex().matches(it) }
            .removePrefix(pythonPackage)
            .trim()
    }

fun sdlTestConfiguration(
    task: PyTest,
    reportXML: String,
    tree: ConfigurableFileTree,
    sdlWorkingDir: File,
    testType: String,
) {
    task.group = "verification"
    task.inputs.property("operatingSystem", OperatingSystem.current().familyName)
    task.inputs.files(tree).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    val report = layout.buildDirectory.file("test-reports/$reportXML.xml")
    task.outputs.file(report)
    task.outputs.cacheIf { true }
    with(task.sources) {
        from(sdlWorkingDir)
        exclude("**")
    }
    task.mark.set(testType)
    task.disableWarnings.set(true)
    task.reportFile.set(report)
    task.pythonExecutable.set("python")
    task.inputs.files(
        rootProject.files("requirements.dev.txt", "requirements.prod.txt", "python-version.txt"),
    )

    val forkedArg = if (isWindows) "" else "--forked"
    val extraArgs = when (testType) {
        "unit" -> "$forkedArg --timeout 60 --cov ./ --cov-report html --cov-config ./.coveragec"
        "system" -> forkedArg
        else -> throw GradleException("Invalid test type: $testType")
    }
    task.extraArgs.set(extraArgs)
    task.outputs.dir(sdlWorkingDir.resolve("coverage"))
}

abstract class MergeTagFilesTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputTagFiles: ListProperty<File>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun mergeFiles() {
        val regex = Regex("""/([^/]+)/target/docker-output/image-repository-tag\.txt""")
        val outputContents = buildString {
            inputTagFiles.get().forEach { file ->
                if (!file.exists()) {
                    return@forEach
                }
                val imageName = regex.find(file.path)?.groupValues?.get(1) ?: ""
                val fileContents = file.readText().trim()
                appendLine("$imageName=$fileContents")
            }
        }
        outputFile.get().asFile.writeText(outputContents)
    }
}