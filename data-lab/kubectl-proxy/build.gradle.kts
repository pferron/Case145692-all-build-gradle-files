import com.seeq.build.ToolchainDownload

plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
}

tasks {
    val downloadKubectl by registering(ToolchainDownload::class) {
        filename.set("kubectl-linux-1.21.1-pkg-v2-universal.tar.gz")
    }

    dockerBuildContext {
        from(
            tarTree(downloadKubectl.map { it.outputs.files.singleFile })
                .filter { it.name == "kubectl" },
        )
        from("$projectDir/patches.kubectl-proxy.txt")
    }

    dockerBuild {
        val seeqMarketingVersion: String by project
        // data-lab server will import a tar file of this image, so it must match the naming scheme in data_lab.py
        datalabServerImage.set("seeq/datalab-kubectl-proxy")
        imageName.set("datalab-kubectl-proxy")
        buildArgs.put("KUBECTL_PROXY_VERSION", seeqMarketingVersion)
    }
}