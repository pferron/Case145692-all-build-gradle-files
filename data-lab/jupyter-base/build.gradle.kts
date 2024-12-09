import com.seeq.build.getJupyterBaseTag
import com.seeq.build.getProperty
import com.seeq.build.isFullySpecifiedTag
import com.seeq.build.isVersionSpecifiedTag
import com.seeq.build.jupyterBase
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
val specifiedBaseImageName: String = getProperty(configFile, "$jupyterBase.name")
val localTag: String = getProperty(configFile, "image.local.tag")
val repositoryTag: String = getProperty(configFile, "image.repository.tag")

val specifiedBaseTag: String = getProperty(project.file("image.properties"), "$jupyterBase.tag")
val jupyterBaseTag = getJupyterBaseTag(specifiedBaseTag, "$jupyterBase.tag")

val tagBaseFile: Configuration by configurations.creating

artifacts {
    add(tagBaseFile.name, project.layout.buildDirectory.dir(localTag).get().asFile)
}

tasks {
    dockerBuildContext {
        from(projectDir) {
            include("R/**")
            include("patches.jupyter.txt")
        }
    }

    dockerPull {
        imageName.set(specifiedBaseImageName)
        registry.set(containerRegistry)
        tags.set(listOf(jupyterBaseTag))
        jupyterImages.set(true)
        imageTagFile.set(project.file(project.layout.buildDirectory.dir(localTag).get().asFile.absolutePath))
    }

    dockerBuild {
        tags.set(listOf(jupyterBaseTag))
        imageName.convention(specifiedBaseImageName)
        imageTagFile.set(project.file(project.layout.buildDirectory.dir(localTag).get().asFile.absolutePath))

        mainlineTagCheck(specifiedBaseTag)
        if (isFullySpecifiedTag(specifiedBaseTag)) {
            dependsOn(dockerPull)
        }

        val isVersionSpecified = isVersionSpecifiedTag(specifiedBaseTag)
        onlyIf {
            isVersionSpecified
        }
    }

    dockerPublish {
        val formattedTimestamp =
            Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")).toString()
        val tag = "$specifiedBaseTag-$formattedTimestamp-SNAPSHOT"
        tags.set(listOf(tag))
        registry.set(containerRegistry)
        imageTagFile.set(project.file(project.layout.buildDirectory.dir(repositoryTag).get().asFile.absolutePath))

        val isVersionSpecified = isVersionSpecifiedTag(specifiedBaseTag)
        onlyIf {
            isVersionSpecified
        }
    }

    dockerRetag {
        tag.set(specifiedBaseTag)
        imageOutputTagFile.set(project.layout.buildDirectory.dir(repositoryTag).get().asFile)
        imageName.convention(specifiedBaseImageName)
        registry.set(containerRegistry)
    }
}