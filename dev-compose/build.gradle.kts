import com.seeq.build.docker.ComposeTask
import com.seeq.build.isWindows
import org.gradle.configurationcache.extensions.serviceOf
import org.jetbrains.kotlin.util.prefixIfNot

plugins {
    com.seeq.build.docker.compose
}

val devLicense by configurations.creating

dependencies {
    devLicense(project(":licensing", "devLicense"))
}

tasks {
    val composeEnvironment by registering(Copy::class) {
        into("$buildDir/compose")
        from(devLicense) {
            into("licenses")
        }
    }

    composeUp {
        val composeEnvironment = composeEnvironment.get()
        inputs.files(composeEnvironment.outputs.files)
        dependsOn(":appserver:appserver-postgres:dockerBuild")
        dependsOn(":appserver:appserver-server:dockerBuild")
        dependsOn(":cache:cache-persistent-service-series-postgres:dockerBuild")
        dependsOn(":cache:cache-persistent-service-series:dockerBuild")
        dependsOn(":cache:cache-persistent-service-quantity-postgres:dockerBuild")
        dependsOn(":cache:cache-persistent-service-quantity:dockerBuild")
        dependsOn(":compute:compute-engine-service:dockerBuild")
        dependsOn(":compute:compute-formula-support-service:dockerBuild")
        dependsOn(":datasource-proxy:datasource-proxy-service:dockerBuild")
        dependsOn(":data-lab:data-lab-jupyter:dockerBuild")
        dependsOn(":data-lab:data-lab-orchestrator:dockerBuild")
        dependsOn(":jvm-link:seeq-link-agent:dockerBuild")
        dependsOn(":messaging:messaging-service-server:dockerBuild")
        dependsOn(":renderer:dockerBuild")
        dependsOn(":reverse-proxy:dockerBuild")
        dependsOn(":emailer:dockerBuild")
        dependsOn(":client:packages-webserver:dockerBuild")
        dependsOn(":genai:dockerBuild")
        dependsOn(":ml-service:dockerBuild")
    }

    withType<ComposeTask>().configureEach {
        composeYaml.set(file("$projectDir/compose.yaml"))
        dotEnv.set(file("$projectDir/.env"))

        // Technically it's the whole path, but most folks create all worktrees as siblings so name is unique.
        // Windows directory names are case-insensitive, so we normalize the name to avoid cross-platform issues.
        val worktree = rootDir.name.toLowerCase()
        projectName.set(worktree)

        validProfiles.addAll(listOf("webserver-dev", "client-dev"))

        env.put("COMPOSE_PROFILES", profiles.map { it.joinToString(",") })
        env.put("COMPOSE_PROJECT_NAME", projectName)

        val seeqMarketingVersion: String by project
        env.putAll(
            mapOf(
                // Appserver Postgres
                "SQ_DATABASE_POSTGRES_ADMINPASSWORD" to "LetAppserverAdminInPlease!",
                "SQ_DATABASE_POSTGRES_ADMINUSERNAME" to "seeq",
                "SQ_DATABASE_POSTGRES_CACHESERVICEPASSWORD" to "LetCacheServiceInPlease!",
                "SQ_DATABASE_POSTGRES_DATABASENAME" to "seeq",
                "SQ_DATABASE_POSTGRES_ROPASSWORD" to "LetAppserverROInPlease!",
                "SQ_DATABASE_POSTGRES_ROUSERNAME" to "seeq_ro",
                "SQ_DATABASE_POSTGRES_RWPASSWORD" to "LetAppserverRWInPlease!",
                "SQ_DATABASE_POSTGRES_RWUSERNAME" to "seeq_rw",
                // Series Cache Postgres
                "SQ_NETWORK_CACHESERVICE_SERIES_POSTGRES_ADMINPASSWORD" to "LetSeriesCacheAdminInPlease!",
                "SQ_NETWORK_CACHESERVICE_SERIES_POSTGRES_ADMINUSERNAME" to "seeq",
                "SQ_NETWORK_CACHESERVICE_SERIES_POSTGRES_DATABASENAME" to "cache",
                "SQ_NETWORK_CACHESERVICE_SERIES_POSTGRES_ROPASSWORD" to "LetSeriesCacheROInPlease!",
                "SQ_NETWORK_CACHESERVICE_SERIES_POSTGRES_ROUSERNAME" to "seeq_ro",
                "SQ_NETWORK_CACHESERVICE_SERIES_POSTGRES_RWPASSWORD" to "LetSeriesCacheRWInPlease!",
                "SQ_NETWORK_CACHESERVICE_SERIES_POSTGRES_RWUSERNAME" to "seeq_rw",
                // Quantity Cache Postgres
                "SQ_NETWORK_CACHESERVICE_QUANTITY_POSTGRES_ADMINPASSWORD" to "LetQuantityCacheAdminInPlease!",
                "SQ_NETWORK_CACHESERVICE_QUANTITY_POSTGRES_ADMINUSERNAME" to "seeq",
                "SQ_NETWORK_CACHESERVICE_QUANTITY_POSTGRES_DATABASENAME" to "cache",
                "SQ_NETWORK_CACHESERVICE_QUANTITY_POSTGRES_ROPASSWORD" to "LetQuantityCacheROInPlease!",
                "SQ_NETWORK_CACHESERVICE_QUANTITY_POSTGRES_ROUSERNAME" to "seeq_ro",
                "SQ_NETWORK_CACHESERVICE_QUANTITY_POSTGRES_RWPASSWORD" to "LetQuantityCacheRWInPlease!",
                "SQ_NETWORK_CACHESERVICE_QUANTITY_POSTGRES_RWUSERNAME" to "seeq_rw",
                // Jvm-Link
                "SQ_FEATURES_AGENTNAMESUFFIX" to "",
                // Proxy
                "SQ_FEATURES_I18N_HOSTNAME" to "",
                // Version
                "SQ_MARKETING_VERSION" to seeqMarketingVersion,
            ),
        )

        // Workaround for CRAB-25415 (figure out a proper licensing mechanism for services)
        env.put("SQ_LICENSE_HOSTNAME", java.net.InetAddress.getLocalHost().hostName.toLowerCase())
        // Webserver Development Server
        env.put(
            "SQ_DEVELOPMENT_SERVER_PORT",
            profiles.map {
                if ("webserver-dev" in it || "client-dev" in it) "34343" else ""
            },
        )

        // Data Lab Orchestrator needs a bind mounted data folder with matching path on the host machine
        // See data-lab/orchestrator/DockerContainerCommon#getContainerParameters
        // CRAB-30579: Use named Docker volumes to avoid all of this complexity
        val dockerFolderMount = if (isWindows) "/run/desktop/mnt/host" else ""
        val datalabDataDir = projectDir.absolutePath
        val datalabDataDirPortable =
            if (isWindows) {
                datalabDataDir
                    .replaceFirst(Regex("(.):"), "$1")
                    .decapitalize()
                    .replace("\\", "/")
                    .prefixIfNot("/")
            } else {
                datalabDataDir
            }
        env.putAll(
            mapOf(
                "SQ_DATALAB_DATA_DIR" to datalabDataDirPortable,
                "SQ_DOCKER_FOLDER_MOUNT" to dockerFolderMount,
            ),
        )

        val exec = serviceOf<ExecOperations>()
        val dataLabFolder = file("data-lab")
        afterClean {
            // CRAB-30579: Data Lab projects use writeable bind mounts instead of named volumes and must be cleaned
            // up manually. This would require root privileges on Linux hosts, so we do it via docker.
            dataLabFolder.listFiles()?.filter { it.name != "keys" }?.let { foldersToRemove ->
                exec.exec {
                    executable("docker")
                    args(
                        "run", "--rm", "-v", "$dataLabFolder:/tmp", "alpine:latest",
                        "rm", "-rf", *foldersToRemove.map { "/tmp/${it.name}" }.toTypedArray(),
                    )
                }
            }
        }
    }
}