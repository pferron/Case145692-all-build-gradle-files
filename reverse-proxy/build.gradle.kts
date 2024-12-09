plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
    com.seeq.build.helm.chart
}

val webserverDist by configurations.creating

dependencies {
    webserverDist(project(":client:packages-webserver", "dist"))
}

tasks {
    dockerBuildContext {
        from(webserverDist) {
            into("dist")
        }
        from("$rootDir/product/nginx-conf") {
            exclude("*.mustache")
            exclude("*.template")
            into("configuration")
        }
        from("$rootDir/product/nginx-conf") {
            include("*.mustache")
            include("*.template")
            into("configuration/templates")
        }
        from("docker-entrypoint.d") {
            into("docker-entrypoint.d")
        }
    }
}