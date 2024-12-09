plugins {
    com.seeq.build.docker.`docker-application`
    com.seeq.build.helm.chart
}

tasks {
    dockerBuildContext {
        from(projectDir) {
            include("go.mod")
            include("go.sum")
            include("cmd/**")
        }
    }
}