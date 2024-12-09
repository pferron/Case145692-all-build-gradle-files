plugins {
    com.seeq.build.docker.`docker-application`
    com.seeq.build.helm.chart
}

tasks {
    dockerBuildContext {
        from(fileTree("$projectDir")) {
            include("requirements.txt", "dev_tenant_manager.py")
        }
        from("$rootDir/common/tenantlib") {
            into("tenantlib")
        }
        from("$rootDir/common/buildlib") {
            into("buildlib")
            include(
                "gh_utils.py",
            )
        }
        from("$rootDir/common/squish") {
            into("squish")
            include(
                "system.py",
                "jiralib.py",
            )
        }
    }
}