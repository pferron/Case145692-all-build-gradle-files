import com.seeq.build.OCIPublishManifestService
import com.seeq.build.docker.DockerPublishTask
import com.seeq.build.isDockerSupported
import com.seeq.build.isLinux

val seeqVersion: String by project

plugins {
    com.seeq.build.base
    com.seeq.build.helm.chart
}

val ociPublishManifestService =
    gradle.sharedServices.registerIfAbsent("ociPublishManifest", OCIPublishManifestService::class) {}

tasks {
    // This image depends on the linux installer image, so the docker image is
    // built using python. See product/build/scripts.py
    register("dockerPublish", DockerPublishTask::class) {
        group = "docker"
        description = "Publish the image to a given repository with the given tags."
        // Because this container depends on the linux installer, it can only be built on linux
        enabled = isDockerSupported && isLinux
        val hashFile = file("${project.rootProject.projectDir}/product/build/image-hash.txt")
        // Ignore this task if `sq image` hasn't produced an image yet. This allows linux
        // developers to be able to run `gradlew dockerPublish ...` without needing to run
        // `sq image` similar to Windows and Mac developers.
        onlyIf {
            hashFile.exists()
        }
        imageHashFile.set(hashFile)
        imageName.set("seeq-server")
        val outputDir = rootProject.buildDir.toPath()

        val ociPublishManifestService = ociPublishManifestService
        usesService(ociPublishManifestService)
        doLast {
            ociPublishManifestService.get().addImage(
                outputDir,
                imageName.get(),
                tags.get()[0],
                imageHashFile.get().asFile.readText(),
            )
        }
    }
}