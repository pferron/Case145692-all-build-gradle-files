plugins {
    com.seeq.build.docker.`docker-application`
    com.seeq.build.helm.chart
}

tasks {
    dockerBuildContext {
        from(fileTree("$projectDir")) {
            include("requirements.txt")
            include("scraper/**")
        }
    }
}