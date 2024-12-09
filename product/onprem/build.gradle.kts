import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.io.Resources
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import java.io.PrintWriter
import java.net.URI
import org.jetbrains.kotlin.incremental.deleteDirectoryContents

data class ImageProperties(
    @JsonProperty("name") var name: String,
    @JsonProperty("tag") var tag: String,
    @JsonProperty("repository") var repository: String? = null,
)

abstract class InstallerBase : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ociManifestFile: RegularFileProperty
}

@CacheableTask
abstract class GenerateInstallerScriptsTask : InstallerBase() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val serviceDeployerImplementationFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val imageLoaderImplementationFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val prereqInstallerImplementationFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependenciesFile: RegularFileProperty

    @get:Input
    abstract val ociRegistry: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val installerFileSource: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val shImplementationFiles: ConfigurableFileTree

    @get:Input
    abstract val airgap: Property<Boolean>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val k3sConfigFile: RegularFileProperty

    @get:OutputFile
    abstract val k3sConfigDeploymentFile: RegularFileProperty

    @get:OutputFile
    abstract val serviceDeployerFile: RegularFileProperty

    @get:OutputFile
    abstract val imageLoaderFile: RegularFileProperty

    @get:OutputFile
    abstract val prereqInstallerFile: RegularFileProperty

    @get:OutputDirectory
    abstract val destinationDir: DirectoryProperty

    @get:OutputFile
    abstract val installerFileDestination: RegularFileProperty

    private fun imageWriter(imageMapList: List<ImageProperties?>, writer: PrintWriter) {
        imageMapList.forEach { image ->
            val inputVarName = image?.name?.uppercase()?.replace("-", "_") + "_IMAGE"
            writer.println("export $inputVarName=\"${image?.name}-${image?.tag}.tar\"")
        }
    }

    private fun writeHeader(writer: PrintWriter) {
        val airgapFlag = if (airgap.get()) {
            "true"
        } else {
            "false"
        }
        writer.println(
            """
            #!/bin/bash
            set -euo pipefail
            IFS=${'$'}'\n\t'
            
            export AIRGAP=$airgapFlag
            
            #This file is auto-generated. Do not edit manually
            
            # Either we're root, or re-run as root.
            [ "${'$'}(id -u)" = 0 ] || exec sudo -n -- "${'$'}0" "${'$'}@"
            """.trimIndent(),
        )
    }

    @TaskAction
    fun generate() {
        if (!ociManifestFile.get().asFile.exists()) {
            throw GradleException("The file ${ociManifestFile.get().asFile.absolutePath} is required to run this task")
        }
        // Ensure the destination is clean before using it
        imageLoaderFile.asFile.get().delete()
        serviceDeployerFile.asFile.get().delete()
        imageLoaderFile.asFile.get().parentFile.mkdirs()

        val manifestContent = ociManifestFile.get().asFile.readText().trimIndent()
        val ociManifest = Configuration.defaultConfiguration().jsonProvider().parse(manifestContent)

        val dependenciesContent = dependenciesFile.get().asFile.readText().trimIndent()
        val dependencies = Configuration.defaultConfiguration().jsonProvider().parse(dependenciesContent)

        val seeqImages: List<String> = JsonPath.read(dependencies, "$.seeqImages")
        val thirdPartyImages: Map<String, Map<String, String>> = JsonPath.read(dependencies, "$.thirdPartyImages")

        // For seeq images, we get the image versions from the oci_manifest
        val imageMapList = seeqImages.map { image ->
            ImageProperties(name = image, tag = JsonPath.read(ociManifest, "$.images.$image.tag"))
        }.toMutableList()

        // For third-party images
        thirdPartyImages.forEach { (imageName, imageProperties) ->
            imageMapList += ImageProperties(name = imageName, tag = imageProperties["tag"].toString())
        }

        imageLoaderFile.get().asFile.printWriter().use { writer ->
            writeHeader(writer)
            writer.println("export REGISTRY=\"${ociRegistry.get()}\"")
            imageWriter(imageMapList, writer)
            writer.println("\n./scripts/image-loader-implementation.sh \"\$@\"")
        }

        val seeqUmbrellaChartName = "seeq-umbrella"
        val seeqUmbrellaVersion: String = JsonPath.read(ociManifest, "$.charts.seeq-umbrella.version")
        val seeqVarName = seeqUmbrellaChartName.uppercase().replace("-", "_")

        val seeqSDLChartName = "seeq-datalab"
        val seeqSDLVersion: String = JsonPath.read(ociManifest, "$.charts.seeq-datalab.version")
        val sdlVarName = seeqSDLChartName.uppercase().replace("-", "_")

        // This is one item for now, but it can be a recursive operation for thirdPartyCharts if needed
        val csiChartName = "csi-driver-nfs"
        val csiChartVersion: String = JsonPath.read(dependencies, "$.thirdPartyCharts.$csiChartName.version")
        val csiVarName = csiChartName.uppercase().replace("-", "_")

        serviceDeployerFile.get().asFile.printWriter().use { writer ->
            writeHeader(writer)
            writer.println("export ${seeqVarName}_CHART=\"$seeqUmbrellaChartName-$seeqUmbrellaVersion.tgz\"")
            writer.println("export ${seeqVarName}_VALUES=\"$seeqUmbrellaChartName-values.yaml\"")
            writer.println("export ${sdlVarName}_CHART=\"$seeqSDLChartName-$seeqSDLVersion.tgz\"")
            writer.println("export ${sdlVarName}_VALUES=\"$seeqSDLChartName-values.yaml\"")
            writer.println("export ${csiVarName}_CHART=\"$csiChartName-$csiChartVersion.tgz\"")
            // writer.println("export ${csiVarName}_VALUES=\"${csiChartName}-values.yaml\"")
            writer.println("\n./scripts/service-deployer-implementation.sh \"\$@\"")
        }

        val prerequisitesContent = dependenciesFile.get().asFile.readText().trimIndent()
        val prerequisites = Configuration.defaultConfiguration().jsonProvider().parse(prerequisitesContent)
        val k3sVersion: String = JsonPath.read(prerequisites, "$.thirdPartyBinaries.k3s")
        val helmVersion: String = JsonPath.read(prerequisites, "$.thirdPartyBinaries.helm")
        prereqInstallerFile.get().asFile.printWriter().use { writer ->
            writeHeader(writer)
            writer.println("export K3s_VERSION=\"$k3sVersion\"")
            writer.println("export HELM_VERSION=\"$helmVersion\"")
            writer.println("\n./scripts/prereq-installer-implementation.sh \"\$@\"")
        }

        shImplementationFiles.forEach { file ->
            file.copyTo(destinationDir.get().asFile.resolve(file.name), overwrite = true)
        }
        installerFileSource.get().asFile.copyTo(installerFileDestination.get().asFile, overwrite = true)
        k3sConfigFile.get().asFile.copyTo(k3sConfigDeploymentFile.get().asFile, overwrite = true)
    }
}

@CacheableTask
abstract class GenerateDefaultValuesFilesTask : InstallerBase() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val valuesTemplateFile: RegularFileProperty

    @get:OutputFile
    abstract val valuesDeploymentFile: RegularFileProperty

    @TaskAction
    fun generate() {
        if (!ociManifestFile.get().asFile.exists()) {
            throw GradleException("The file ${ociManifestFile.get().asFile.absolutePath} is required to run this task")
        }
        val manifestContent = ociManifestFile.get().asFile.readText().trimIndent()
        val parsedManifest = Configuration.defaultConfiguration().jsonProvider().parse(manifestContent)
        val regex = "##(.+?)##".toRegex()

        var valuesYamlContent = valuesTemplateFile.get().asFile.readText().trimIndent().replace(regex) { matchResult ->
            val valueQuery = matchResult.groups[1]?.value.toString().trimStart().trimEnd()
            val value = JsonPath.read<String>(parsedManifest, "$$valueQuery")
            "$value  # $valueQuery"
        }

        valuesDeploymentFile.get().asFile.writeText(valuesYamlContent)
    }
}

@CacheableTask
abstract class SaveChartForInstaller @Inject constructor(private val fs: FileSystemOperations) :
    InstallerBase() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val chartSourceDirectory: DirectoryProperty

    @get:Input
    abstract val chartName: Property<String>

    @get:OutputDirectory
    abstract val chartOutputDir: DirectoryProperty

    @TaskAction
    fun saveChart() {
        val manifestContent = ociManifestFile.get().asFile.readText().trimIndent()
        val parsedManifest = Configuration.defaultConfiguration().jsonProvider().parse(manifestContent)

        val chartVersion: String = JsonPath.read(parsedManifest, "$.charts.${chartName.get()}.version")
        val fileName = "${chartName.get()}-$chartVersion.tgz"
        val chartSourceFile = chartSourceDirectory.file(fileName).get().asFile

        fs.copy {
            from(chartSourceFile)
            into(chartOutputDir.get().asFile)
        }
    }
}

@CacheableTask
abstract class SaveImagesForInstallerTask @Inject constructor(private val exec: ExecOperations) : InstallerBase() {
    @get:OutputDirectory
    abstract val imagesDestination: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependenciesFile: RegularFileProperty

    @get:Input
    abstract val seeqRegistry: Property<String>

    @get:Input
    abstract val ociRegistry: Property<String>

    private fun dockerPull(imageMapList: List<ImageProperties>, reTagImage: Boolean = false) {
        imageMapList.forEach { imageProperties ->
            var imagePath = "${imageProperties.repository}/${imageProperties.name}:${imageProperties.tag}"

            println("Saving: $imagePath")
            exec.exec {
                commandLine(listOf("docker", "pull", imagePath))
            }
            if (reTagImage) {
                val ociPath = "${ociRegistry.get()}/images/${imageProperties.name}:${imageProperties.tag}"
                exec.exec {
                    commandLine(listOf("docker", "image", "tag", imagePath, ociPath))
                }
                imagePath = ociPath
            }

            exec.exec {
                commandLine(
                    listOf(
                        "docker", "image", "save", imagePath, "-o",
                        imagesDestination.get().asFile.resolve("${imageProperties.name}-${imageProperties.tag}.tar"),
                    ),
                )
            }
        }
    }

    @TaskAction
    fun saveImages() {
        if (!ociManifestFile.get().asFile.exists()) {
            throw GradleException("The file ${ociManifestFile.get().asFile.absolutePath} is required to run this task")
        }

        imagesDestination.asFile.get().deleteDirectoryContents()
        val manifestContent = ociManifestFile.get().asFile.readText().trimIndent()
        val ociManifest = Configuration.defaultConfiguration().jsonProvider().parse(manifestContent)
        val subChartImagesContent = dependenciesFile.get().asFile.readText().trimIndent()
        val subChartImages = Configuration.defaultConfiguration().jsonProvider().parse(subChartImagesContent)

        val seeqImages: List<String> = JsonPath.read(subChartImages, "$.seeqImages")
        val thirdPartyImages: Map<String, Map<String, String>> = JsonPath.read(subChartImages, "$.thirdPartyImages")

        // For seeq images, we get the image versions from the oci_manifest
        val seeqImageMapList = seeqImages.map { image ->
            ImageProperties(
                name = image,
                tag = JsonPath.read(ociManifest, "$.images.$image.tag"),
                repository = seeqRegistry.get() + "/images",
            )
        }

        // For third-party images
        val thirdPartyImageList = thirdPartyImages.map { (imageName, imageProperties) ->
            ImageProperties(
                name = imageName,
                tag = imageProperties["tag"].toString(),
                repository = imageProperties["repository"].toString(),
            )
        }

        dockerPull(seeqImageMapList, reTagImage = true)
        dockerPull(thirdPartyImageList)
    }
}

@CacheableTask
abstract class SavePrerequisitesForInstallerTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependenciesFile: RegularFileProperty

    @get:OutputDirectory
    abstract val imagesDestination: DirectoryProperty

    @get:OutputDirectory
    abstract val chartOutputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val binariesDestination: DirectoryProperty

    @get:OutputDirectory
    abstract val k3sInstallerDestination: DirectoryProperty

    private fun download(url: String, outputDir: DirectoryProperty, filename: String) {
        outputDir.get().asFile.resolve(filename).outputStream().use {
            Resources.copy(URI(url).toURL(), it)
        }
    }

    @TaskAction
    fun savePrerequisites() {
        val prerequisitesContent = dependenciesFile.get().asFile.readText().trimIndent()
        val prerequisites = Configuration.defaultConfiguration().jsonProvider().parse(prerequisitesContent)

        val k3sVersion: String = JsonPath.read<String?>(prerequisites, "$.thirdPartyBinaries.k3s")
            .replace("+", "%2B")
        val helmVersion: String = JsonPath.read(prerequisites, "$.thirdPartyBinaries.helm")

        println("Saving the K3s image")
        val k3sImageUrl = "https://github.com/k3s-io/k3s/releases/download/v$k3sVersion/k3s-airgap-images-amd64.tar"
        download(k3sImageUrl, imagesDestination, "k3s-airgap-images-amd64.tar")

        println("Saving the K3s binaries")
        val k3sBinaryUrl = "https://github.com/k3s-io/k3s/releases/download/v$k3sVersion/k3s"
        download(k3sBinaryUrl, binariesDestination, "k3s")

        println("Saving the K3s installer")
        val k3sInstallerUrl = "https://get.k3s.io/"
        download(k3sInstallerUrl, k3sInstallerDestination, "k3s-installer.sh")

        println("Saving the Helm binaries")
        val helmBinaryUrl = "https://get.helm.sh/helm-v$helmVersion-linux-amd64.tar.gz"
        download(helmBinaryUrl, binariesDestination, "helm-v$helmVersion-linux-amd64.tar.gz")

        // This is one item for now, but it can be a recursive operation for thirdPartyCharts if needed
        val chartName = "csi-driver-nfs"
        val chartVersion: String = JsonPath.read(prerequisites, "$.thirdPartyCharts.$chartName.version")
        val chartRepo: String = JsonPath.read(prerequisites, "$.thirdPartyCharts.$chartName.repository")
        val chartUrl = "$chartRepo/$chartVersion/$chartName-$chartVersion.tgz"
        download(chartUrl, chartOutputDir, "$chartName-$chartVersion.tgz")
    }
}

plugins {
    com.seeq.build.base
}

tasks {

    val generateInstallerScriptsStandard by registering(GenerateInstallerScriptsTask::class) {
        airgap.set(false)
        destinationDir.set(layout.buildDirectory.dir("standard-files/seeq-k3s-installer/scripts"))
        serviceDeployerFile.set(
            layout.buildDirectory
                .file("standard-files/seeq-k3s-installer/scripts/service-deployer.sh"),
        )
        imageLoaderFile.set(
            layout.buildDirectory
                .file("standard-files/seeq-k3s-installer/scripts/image-loader.sh"),
        )
        prereqInstallerFile.set(
            layout.buildDirectory
                .file("standard-files/seeq-k3s-installer/scripts/prereq-installer.sh"),
        )
        installerFileDestination.set(layout.buildDirectory.file("standard-files/seeq-k3s-installer/installer.sh"))
        k3sConfigDeploymentFile.set(
            layout.buildDirectory
                .file("standard-files/seeq-k3s-installer/values/config.yaml"),
        )
    }

    val generateInstallerScriptsAirgap by registering(GenerateInstallerScriptsTask::class) {
        airgap.set(true)
        destinationDir.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/scripts"))
        serviceDeployerFile.set(
            layout.buildDirectory
                .file("airgap-files/seeq-k3s-installer/scripts/service-deployer.sh"),
        )
        imageLoaderFile.set(layout.buildDirectory.file("airgap-files/seeq-k3s-installer/scripts/image-loader.sh"))
        prereqInstallerFile.set(
            layout.buildDirectory
                .file("airgap-files/seeq-k3s-installer/scripts/prereq-installer.sh"),
        )
        installerFileDestination.set(layout.buildDirectory.file("airgap-files/seeq-k3s-installer/installer.sh"))
        k3sConfigDeploymentFile.set(
            layout.buildDirectory
                .file("airgap-files/seeq-k3s-installer/values/config.yaml"),
        )
    }

    withType<GenerateInstallerScriptsTask>().configureEach {
        group = "installer-package"
        description = """
            Generates the prereq-installer.sh, image-loader.sh, and service-deployer.sh script to install 
            the k3s seeq resources
        """.trimIndent()
        serviceDeployerImplementationFile.set(project.file("service-deployer-implementation.sh"))
        imageLoaderImplementationFile.set(project.file("image-loader-implementation.sh"))
        prereqInstallerImplementationFile.set(project.file("prereq-installer-implementation.sh"))
        ociManifestFile.set(rootProject.layout.buildDirectory.file("oci_publish_manifest.json"))
        ociRegistry.set("oci.seeq.com")
        dependenciesFile.set(File(projectDir, "dependencies.json"))
        shImplementationFiles.setDir(projectDir).include("**/*.sh").exclude("installer.sh")
            .exclude("target/**")

        installerFileSource.set(project.file("installer.sh"))
        k3sConfigFile.set(project.file("values/config.yaml"))
    }

    val generateSeeqValuesFileStandard by registering(GenerateDefaultValuesFilesTask::class) {
        valuesDeploymentFile.set(
            layout.buildDirectory
                .file("standard-files/seeq-k3s-installer/values/seeq-umbrella-values.yaml"),
        )
        valuesTemplateFile.set(project.file("values/seeq-umbrella-values-template.yaml"))
    }

    val generateSeeqValuesFileAirgap by registering(GenerateDefaultValuesFilesTask::class) {
        valuesDeploymentFile.set(
            layout.buildDirectory
                .file("airgap-files/seeq-k3s-installer/values/seeq-umbrella-values.yaml"),
        )
        valuesTemplateFile.set(project.file("values/seeq-umbrella-values-template.yaml"))
    }

    val generateSDLValuesFileStandard by registering(GenerateDefaultValuesFilesTask::class) {
        valuesDeploymentFile.set(
            layout.buildDirectory
                .file("standard-files/seeq-k3s-installer/values/seeq-datalab-values.yaml"),
        )
        valuesTemplateFile.set(project.file("values/seeq-datalab-values-template.yaml"))
    }

    val generateSDLValuesFileAirgap by registering(GenerateDefaultValuesFilesTask::class) {
        valuesDeploymentFile.set(
            layout.buildDirectory
                .file("airgap-files/seeq-k3s-installer/values/seeq-datalab-values.yaml"),
        )
        valuesTemplateFile.set(project.file("values/seeq-datalab-values-template.yaml"))
    }

    withType<GenerateDefaultValuesFilesTask>().configureEach {
        group = "installer-package"
        description = "Generates the values.yaml file with default values for the umbrella-chart"
        ociManifestFile.set(rootProject.layout.buildDirectory.file("oci_publish_manifest.json"))
    }

    val saveUmbrellaChartForStandardInstaller by registering(SaveChartForInstaller::class) {
        chartOutputDir.set(layout.buildDirectory.dir("standard-files/seeq-k3s-installer/charts"))
        chartName.set("seeq-umbrella")
        chartSourceDirectory.set(
            project(":product:umbrella-chart").layout.buildDirectory
                .dir("helm/packaged-charts"),
        )
        dependsOn(":product:umbrella-chart:helmPackage")
    }

    val saveUmbrellaChartForAirgapInstaller by registering(SaveChartForInstaller::class) {
        chartOutputDir.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/charts"))
        chartName.set("seeq-umbrella")
        chartSourceDirectory.set(
            project(":product:umbrella-chart").layout.buildDirectory
                .dir("helm/packaged-charts"),
        )
        dependsOn(":product:umbrella-chart:helmPackage")
    }

    val saveSDLChartForStandardInstaller by registering(SaveChartForInstaller::class) {
        chartOutputDir.set(layout.buildDirectory.dir("standard-files/seeq-k3s-installer/charts"))
        chartName.set("seeq-datalab")
        chartSourceDirectory.set(
            project(":data-lab:data-lab-kubernetes-seeq-datalab").layout.buildDirectory
                .dir("helm/packaged-charts"),
        )
        dependsOn(":data-lab:data-lab-kubernetes-seeq-datalab:helmPackage")
    }

    val saveSDLChartForAirgapInstaller by registering(SaveChartForInstaller::class) {
        chartOutputDir.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/charts"))
        chartName.set("seeq-datalab")
        chartSourceDirectory.set(
            project(":data-lab:data-lab-kubernetes-seeq-datalab").layout.buildDirectory
                .dir("helm/packaged-charts"),
        )
        dependsOn(":data-lab:data-lab-kubernetes-seeq-datalab:helmPackage")
    }

    withType<SaveChartForInstaller>().configureEach {
        group = "installer-package"
        description =
            "Gets the seeq-umbrella-chart.tgz and copies it to product/onprem/target/<standard/airgap>-files/charts"
        ociManifestFile.set(rootProject.layout.buildDirectory.file("oci_publish_manifest.json"))
    }

    val saveImagesForAirgapInstaller by registering(SaveImagesForInstallerTask::class) {
        group = "installer-package"
        description = "Gets the images required to install seeq-umbrella chart and copies them to " +
            "target/onprem/images folder"
        ociManifestFile.set(rootProject.layout.buildDirectory.file("oci_publish_manifest.json"))
        dependenciesFile.set(File(projectDir, "dependencies.json"))
        imagesDestination.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/images"))
        seeqRegistry.set("seeq13.azurecr.io")
        ociRegistry.set("oci.seeq.com")
    }

    val savePrerequisitesForAirgapInstaller by registering(SavePrerequisitesForInstallerTask::class) {
        group = "installer-package"
        description = "Gets the k3s and helm dependencies and copies them to the target/onprem/binaries folder"
        dependenciesFile.set(File(projectDir, "dependencies.json"))
        imagesDestination.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/images"))
        chartOutputDir.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/charts"))
        binariesDestination.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/binaries"))
        k3sInstallerDestination.set(layout.buildDirectory.dir("airgap-files/seeq-k3s-installer/scripts"))
    }

    register<Tar>("packageStandardInstaller") {
        description = "Creates a tarball with the onprem standard installer"
        archiveFileName.set("installer.tar.gz")
        destinationDirectory.set(layout.buildDirectory.dir("standard-installer"))
        from(layout.buildDirectory.dir("standard-files"))
        dependsOn(saveUmbrellaChartForStandardInstaller)
        dependsOn(saveSDLChartForStandardInstaller)
        dependsOn(generateSeeqValuesFileStandard)
        dependsOn(generateSDLValuesFileStandard)
        dependsOn(generateInstallerScriptsStandard)
    }

    register<Tar>("packageAirgapInstaller") {
        description = "Creates a tarball with the onprem airgap installer"
        archiveFileName.set("installer.tar.gz")
        destinationDirectory.set(layout.buildDirectory.dir("airgap-installer"))
        from(layout.buildDirectory.dir("airgap-files"))
        dependsOn(saveImagesForAirgapInstaller)
        dependsOn(saveUmbrellaChartForAirgapInstaller)
        dependsOn(saveSDLChartForAirgapInstaller)
        dependsOn(generateSeeqValuesFileAirgap)
        dependsOn(generateSDLValuesFileAirgap)
        dependsOn(generateInstallerScriptsAirgap)
        dependsOn(savePrerequisitesForAirgapInstaller)
    }

    withType<Tar>().configureEach {
        group = "installer-package"
        compression = Compression.GZIP

        filePermissions {
            user.execute = true
        }
    }
}