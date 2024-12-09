plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
    com.seeq.build.helm.chart
}

description = "appserver-postgres"

tasks {
    dockerBuildContext {
        from("$rootDir/common/misc/templates/postgresql/conf") {
            include("postgresql.seeq-defaults.conf")
            include("postgresql.seeq-posix.conf")
        }
        from(projectDir) {
            include("postgresql.conf")
            include("docker-entrypoint.d/**")
        }
    }
}