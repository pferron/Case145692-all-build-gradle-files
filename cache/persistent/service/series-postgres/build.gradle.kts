plugins {
    com.seeq.build.base
    com.seeq.build.docker.docker
}

description = "cache-service-series-postgres"

tasks {
    dockerBuildContext {
        from("$rootDir/common/buildlib") {
            include("postgres-init-cache.sql")
        }
        from("$rootDir/common/misc/templates/postgresql/conf") {
            include("postgresql.seeq-defaults.conf")
            include("postgresql.seeq-posix.conf")
        }
        from(projectDir) {
            include("postgres-init.sh")
            include("postgresql.conf")
        }
    }
}

tasks {
    dockerBuild {
        imageName.set("series-cache-postgres")
    }
}