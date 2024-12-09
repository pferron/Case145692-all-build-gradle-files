import com.seeq.build.getJupyterBaseTag
import com.seeq.build.getProperty
import com.seeq.build.isFullySpecifiedTag
import com.seeq.build.isVersionSpecifiedTag
import com.seeq.build.jupyterBase
import com.seeq.build.jupyterPackages
import com.seeq.build.mainlineTagCheck
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
}

val configFile = project(":data-lab").layout.projectDirectory.dir("gradle.tasks.properties").asFile
val containerRegistry: String = getProperty(configFile, "registry")
val specifiedPackagesImageName: String = getProperty(configFile, "$jupyterPackages.name")
val localTag: String = getProperty(configFile, "image.local.tag")
val repositoryTag: String = getProperty(configFile, "image.repository.tag")

val specifiedPackagesTag: String = getProperty(project.file("image.properties"), "$jupyterPackages.tag")
val jupyterPackagesTag = getJupyterBaseTag(specifiedPackagesTag, "$jupyterPackages.tag")

val specifiedBaseImageName: String = getProperty(configFile, "$jupyterBase.name")

val specifiedBaseTag: String = getProperty(
    project(":data-lab:data-lab-jupyter-base").layout.projectDirectory.dir("image.properties").asFile,
    "$jupyterBase.tag",
)
val jupyterBaseTag = getJupyterBaseTag(specifiedBaseTag, "$jupyterBase.tag")

val tagBaseFile: Configuration by configurations.creating
val tagPackagesFile: Configuration by configurations.creating

dependencies {
    tagBaseFile(project(":data-lab:data-lab-jupyter-base", "tagBaseFile"))
}

artifacts {
    add(tagPackagesFile.name, project.layout.buildDirectory.dir(localTag).get().asFile)
}

tasks {
    dockerBuildContext {
        from(projectDir) {
            include("isotree/**")
            include("seeq-datalab-requirements/**")
            include("delete-keys.jupyter.txt")
        }
    }

    dockerPull {
        imageName.set(specifiedPackagesImageName)
        registry.set(containerRegistry)
        tags.set(listOf(jupyterPackagesTag))
        jupyterImages.set(true)
        imageTagFile.set(project.file(project.layout.buildDirectory.dir(localTag).get().asFile.absolutePath))
    }

    dockerBuild {
        imageName.convention(specifiedPackagesImageName)
        imageTagFile.set(project.layout.buildDirectory.dir(localTag).get().asFile)

        tags.set(listOf(jupyterPackagesTag))

        mainlineTagCheck(specifiedBaseTag)
        mainlineTagCheck(specifiedPackagesTag)

        if (isFullySpecifiedTag(specifiedBaseTag) && isFullySpecifiedTag(specifiedPackagesTag)) {
            dependsOn(dockerPull)
        }

        if (!isFullySpecifiedTag(specifiedBaseTag) && isFullySpecifiedTag(specifiedPackagesTag)) {
            throw GradleException(
                "Version in ../jupyter-packages/image.properties cannot be fully specified " +
                    "if version in ../jupyter-base/image.properties is not fully specified",
            )
        }

        if (isFullySpecifiedTag(specifiedBaseTag)) {
            buildArgs.put("BASE_IMAGE", "$containerRegistry/$specifiedBaseImageName")
        } else {
            buildArgs.put("BASE_IMAGE", specifiedBaseImageName)
        }

        buildArgs.put("POETRY_VERSION", pythonPackageVersion("poetry"))
        baseImageTagFile.fileProvider(tagBaseFile.elements.map { it.single().asFile })

        val isVersionSpecified = isVersionSpecifiedTag(specifiedPackagesTag)
        onlyIf {
            isVersionSpecified
        }
        dependsOn(":data-lab:data-lab-jupyter-base:dockerBuild")
    }

    dockerPublish {
        val formattedTimestamp =
            Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")).toString()
        val tag = "$specifiedPackagesTag-$formattedTimestamp-SNAPSHOT"
        tags.set(listOf(tag))
        registry.set(containerRegistry)
        imageTagFile.set(project.file(project.layout.buildDirectory.dir(repositoryTag).get().asFile.absolutePath))

        val isVersionSpecified = isVersionSpecifiedTag(specifiedPackagesTag)
        dependsOn(":data-lab:data-lab-jupyter-base:dockerPublish")
        onlyIf {
            isVersionSpecified
        }
    }

    dockerRetag {
        tag.set(specifiedPackagesTag)
        imageOutputTagFile.set(project.layout.buildDirectory.dir(repositoryTag).get().asFile)
        imageName.convention(specifiedPackagesImageName)
        registry.set(containerRegistry)
        dependsOn(":data-lab:data-lab-jupyter-base:dockerRetag")
    }
}

fun pythonPackageVersion(pythonPackage: String) =
    providers.exec {
        commandLine("pip", "list")
    }.standardOutput.asText.map { output ->
        output.lines().single { """^${Regex.escape(pythonPackage)}\s+\S+$""".toRegex().matches(it) }
            .removePrefix(pythonPackage).trim()
    }