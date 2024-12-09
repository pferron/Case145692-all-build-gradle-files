import com.seeq.build.npm.Jest

plugins {
    com.seeq.build.npm.`node-module`
    com.seeq.build.docker.docker
}

tasks {
    val appSources = fileTree("$projectDir") {
        include("*.js")
        exclude("jest.config.js")
        include("api-errors/**")
        include("public/**")
        include("static-messages/**")
    }

    withType<Jest> {
        roots.add("test")

        inputs.files(appSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    }

    test {
        args.add("--coverage") // Coverage limits are set in jest.config.js
    }

    dockerBuildContext {
        from(appSources)
        from(projectDir) {
            include("start_orchestrator.sh")
            include("patches.orchestrator.txt")
            include("delete-keys.orchestrator.txt")
            include("package.json")
            include("package-lock.json")
            include(".npmrc")
        }
    }

    dockerBuild {
        val seeqMarketingVersion: String by project
        // data-lab server will import a tar file of this image, so it must match the naming scheme in data_lab.py
        datalabServerImage.set("seeq/datalab-orchestrator")
        imageName.set("datalab-orchestrator")
        buildArgs.put("ORCHESTRATOR_VERSION", seeqMarketingVersion)
    }
}