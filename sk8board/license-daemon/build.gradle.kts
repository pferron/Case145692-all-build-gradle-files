plugins {
    com.seeq.build.docker.`docker-application`
}

tasks {
    dockerBuildContext {
        from(fileTree("$projectDir")) {
            include("requirements.txt", "license_daemon.py")
        }
        from("$rootDir/common/squish") {
            into("squish")
            include(
                "licensing.py",
                "salesforcelib.py",
                "gptservice.py",
                "keyvault.py",
                "system.py",
                "store.py",
                "tablelib.py",
                "s3_readwrite.py",
            )
        }
    }
}