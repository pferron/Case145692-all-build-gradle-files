import com.seeq.build.ToolchainDownload

plugins {
    com.seeq.build.appserver.`appserver-module`
    com.seeq.build.docker.`docker-application`
}

description = "appserver-server"

val downloadLicenseClient by tasks.registering(ToolchainDownload::class) {
    filename.set("ezclient-12.0BL4-linux.tar.gz")
}

val pythonPackages by configurations.creating

dependencies {
    api(project(":seriesdata"))

    implementation(project(":seeq:common-concurrent"))

    implementation(project(":appserver:appserver-authentication"))
    implementation(project(":appserver:appserver-corelib"))
    implementation(project(":appserver:appserver-drivers-condition"))
    implementation(project(":appserver:appserver-drivers-graph"))
    implementation(project(":appserver:appserver-drivers-signal-file"))
    implementation(project(":appserver:appserver-datalayer"))
    implementation(project(":appserver:appserver-jobs"))
    implementation(project(":appserver:appserver-messaging"))
    implementation(project(":appserver:appserver-query"))
    implementation(project(":appserver:appserver-compute"))
    implementation(project(":appserver:appserver-grpc"))
    implementation(project(":appserver:appserver-tables"))
    implementation(project(":cache:cache-pipeline-quantity"))
    implementation(project(":cache:cache-pipeline-series"))
    implementation(project(":datasource-proxy:datasource-proxy-client"))

    implementation(project(":seeq-sdk")) {
        because("Emailer communicates with the emailer service")
    }

    implementation(project(":messaging:messaging-core"))
    implementation(project(":messaging:messaging-databind"))

    implementation(project(":messaging:messaging-service-core"))
    implementation(project(":messaging:messaging-service-websocket"))

    implementation("com.github.ua-parser:uap-java:1.5.0")

    implementation(project(":cache:cache-persistent-client"))
    implementation(project(":cache:cache-quantity-scalar"))
    implementation(project(":cache:cache-quantity-content"))
    implementation(project(":seeq:common-grpc-unhandled-exception"))
    implementation(project(":seeq:common-guice-to-hk2-binder"))

    implementation("io.swagger.core.v3:swagger-annotations:2.1.10")
    implementation("io.swagger:swagger-jersey2-jaxrs:1.5.16")
    implementation("org.glassfish.grizzly:grizzly-http-server")
    implementation("org.glassfish.grizzly:grizzly-http-servlet")
    implementation("org.glassfish.grizzly:grizzly-framework")
    implementation("org.glassfish.grizzly:grizzly-core")
    implementation("org.glassfish.grizzly:grizzly-websockets")
    implementation("org.glassfish.grizzly:grizzly-http2:2.4.4")
    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson")
    implementation("org.glassfish.jersey.media:jersey-media-multipart")
    implementation("org.glassfish.jersey.inject:jersey-hk2")
    implementation("javax.servlet:javax.servlet-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(group = "com.googlecode.json-simple", name = "json-simple", version = "1.1.1") {
        exclude(group = "junit", module = "junit")
    }
    implementation("com.opencsv:opencsv")
    implementation("com.github.tony19:named-regexp")
    implementation("io.dropwizard.metrics:metrics-annotation:3.2.6")
    implementation("io.dropwizard.metrics:metrics-jersey2:3.2.6")
    implementation("org.flywaydb:flyway-core:8.5.13")
    implementation("org.postgresql:postgresql")
    implementation("uk.org.lidalia:sysout-over-slf4j")
    implementation("org.jsoup:jsoup")
    implementation("org.apache.olingo:olingo-odata2-jpa-processor-api:2.0.11")
    implementation("org.apache.olingo:olingo-odata2-jpa-processor-core:2.0.11")
    implementation("org.apache.olingo:olingo-odata2-api-annotation:2.0.11")
    implementation("org.apache.olingo:odata-commons-api:4.10.0")
    implementation("org.apache.olingo:odata-commons-core:4.10.0")
    implementation("org.apache.olingo:odata-server-core:4.10.0")
    implementation("org.apache.olingo:odata-server-api:4.10.0")
    implementation("org.reflections:reflections")
    implementation("com.expediagroup:graphql-kotlin-server")
    implementation("javax.xml.bind:jaxb-api:2.3.1") {
        because("Old version is needed until Jersey is upgraded")
    }
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1") {
        because("CRAB-41054: Needed to prevent jOOQ code from throwing warnings. See: https://github.com/jOOQ/jOOQ/issues/14865")
    }

    runtimeOnly("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0") {
        because("Adds wrapping appenders that log trace_id and span_id")
    }

    testImplementation(project(":compute:compute-formula-support-contract"))
    testImplementation(testFixtures(project(":appserver:appserver-drivers-graph")))
    testImplementation("io.grpc:grpc-netty-shaded")
    testImplementation("org.jboss.resteasy:resteasy-multipart-provider:3.1.2.Final")
    testImplementation("com.jayway.jsonpath:json-path")
    testImplementation("org.glassfish.tyrus.bundles:tyrus-standalone-client-jdk")
    testImplementation("org.glassfish.tyrus:tyrus-container-grizzly-server")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation("org.apache.poi:poi-ooxml")
    testImplementation("io.grpc:grpc-testing")
    testImplementation("org.apache.olingo:odata-client-api:4.10.0")
    testImplementation("org.apache.olingo:odata-client-core:4.10.0")

    testFixturesApi(project(":seeq-sdk"))
    testFixturesImplementation(project(":licensing", "devLicense"))
    testFixturesApi("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesApi(project(":cache:cache-persistent-client"))
    testFixturesApi("org.jboss.resteasy:resteasy-client")
    testFixturesApi(testFixtures(project(":appserver:appserver-datalayer")))
    testFixturesApi(testFixtures(project(":appserver:appserver-corelib")))
    testFixturesApi(testFixtures(project(":appserver:appserver-analytics")))
    testFixturesApi(testFixtures(project(":appserver:appserver-query")))
    testFixturesApi(testFixtures(project(":seriesdata")))
    testFixturesApi(project(":appserver:appserver-messaging"))
    testFixturesApi("org.glassfish.tyrus.bundles:tyrus-standalone-client-jdk")
    testFixturesApi("org.glassfish.tyrus:tyrus-container-grizzly-server")
    testFixturesApi("org.glassfish.grizzly:grizzly-websockets")

    testFixturesImplementation(project(":appserver:appserver-server"))
    testFixturesImplementation(project(":appserver:appserver-drivers-graph"))
    testFixturesImplementation(project(":datasource-proxy:datasource-proxy-client"))
    testFixturesImplementation(project(":cache:cache-pipeline-quantity"))
    testFixturesImplementation("io.grpc:grpc-netty-shaded")
    testFixturesImplementation("io.grpc:grpc-testing")
    testFixturesImplementation("io.kotest:kotest-assertions-core-jvm")

    pythonPackages(project(":seeq-sdk", "pyPiPackage"))
    pythonPackages(project(":seeq-sdk", "pyPiPackageSpy"))
    pythonPackages(rootProject.files("common/configuration"))
}

application {
    mainClass.set("com.seeq.appserver.Main")
}

coverage {
    threshold.set(0.40)
    excludes.addAll("**/*Container.class")
}

tasks {
    dockerBuildContext {
        from(tarTree(downloadLicenseClient.map { it.outputs.files.singleFile })) {
            into("ezclient")
        }
        // Copy pilot and python packages that it depends on so that PilotCommand works
        // Temporary until we determine a better containerized configuration strategy
        from(file("$rootDir/requirements.prod.txt"))
        from("$rootDir/product/pilot") {
            into("python/pilot")
            include("**/*.py")
            exclude("**/test_*", "**/tests/**")
        }
        from("$rootDir/common/buildlib") {
            into("python/pilot/buildlib")
            include("**/*.py", "**/*.sql")
            exclude("**/test_*", "**/tests/**")
        }
        from("$rootDir/common/squish") {
            into("python/pilot/squish")
            include("**/*.py", "**/*.sql")
            exclude("**/test_*", "**/tests/**")
        }
        from("$rootDir/common/configuration") {
            into("python/pilot/configuration")
            include("**/*.py")
            exclude("**/test_*", "**/tests/**")
        }
        from("$rootDir/common/migrations") {
            into("python/pilot/migrations")
            include("**/*.py")
            exclude("**/test_*", "**/tests/**")
        }
        from(pythonPackages) {
            into("python/seeq")
            exclude("**/test_*", "**/tests/**")
        }
    }
    dockerBuild {
        imageName.set("appserver")
    }

    register<Task>("generateDbBaseline") {
        group = "build"
        description = "Creates a flyway baseline for the database. Requires that flyway has completed all " +
            "preinitialize migrations and the database is running."
        doLast {
            val migrationDir = "$projectDir/src/main/kotlin/com/seeq/appserver/migrations/preinitialize"
            var baselineVersion = fileTree(migrationDir).files
                .mapNotNull {
                    it.name.split("__").firstOrNull()?.let { f ->
                        if (f.first() == 'V') {
                            f.substring(1)
                        } else {
                            null
                        }
                    }
                }
                .maxOfOrNull(String::toLong) ?: 20220119000000L
            baselineVersion += 1 // Without this, ITs will complain that two migrations exist with the same version.
            val migrationFilename = baselineVersion.let { "B${it}__auto_generated_baseline.kt" }
            val env = System.getenv().toMutableMap()
            env["PGPASSWORD"] = File("$rootDir/sq-run-data-dir/keys/postgres_admin.key").readText()
            val binExtension = if (System.getProperties().getProperty("os.name").lowercase().contains("windows")) {
                ".exe"
            } else {
                ""
            }
            val outputFile = "$migrationDir/$migrationFilename"

            val dumpOutput = org.apache.commons.io.output.ByteArrayOutputStream()

            // Dumps roles
            exec {
                workingDir = file(migrationDir)
                executable =
                    "$rootDir/dependencies/postgres/${env["SQ_ARCHITECTURE"]}/files/bin/pg_dumpall" + binExtension
                args = listOf(
                    "-h", "127.0.0.1",
                    "-p", "34215",
                    "-U", "seeq",
                    "--no-role-passwords",
                    "--roles-only",
                )
                standardOutput = dumpOutput
                environment = env as Map<String, Any>?
            }

            // Dumps DB Schema
            exec {
                workingDir = file(migrationDir)
                executable =
                    "$rootDir/dependencies/postgres/${env["SQ_ARCHITECTURE"]}/files/bin/pg_dump" + binExtension
                args = listOf(
                    "-h", "127.0.0.1",
                    "-p", "34215",
                    "-U", "seeq",
                    "--create",
                    "--schema-only",
                    "--no-comments",
                    "--dbname=seeq",
                    "--exclude-table=flyway_schema_history*",
                )
                standardOutput = dumpOutput
                environment = env as Map<String, Any>?
            }
            var fullDump = String(dumpOutput.toByteArray())

            // Fix line endings on Windows
            Regex("(\\r|\\n|\\r\\n){2,}").replace(fullDump, "\n")

            // Remove postgresql-specific command /connect
            fullDump = Regex("\n\\\\connect (\\S+)").replace(fullDump, "")

            // Remove & replace public schema permissions queries for readonly and readwrite
            // If not removed, these queries cause problems during ITs
            fullDump = Regex("GRANT [a-zA-Z,]+ ON TABLE public\\.[a-zA-Z_]+ TO (readonly|readwrite);")
                .replace(fullDump, "-- Removed GRANT query on public schema")
            fullDump += """
                -- Create "readonly" and "readwrite" roles that cannot modify roles or drop tables
                ALTER ROLE readonly WITH INHERIT NOCREATEROLE NOCREATEDB NOLOGIN NOBYPASSRLS;
                GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly;
                ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly;
                ALTER ROLE readwrite WITH INHERIT NOCREATEROLE NOCREATEDB NOLOGIN NOBYPASSRLS;
                GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA public TO readwrite;
                ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, UPDATE, INSERT, DELETE ON TABLES TO readwrite;
            """.trimIndent() + '\n'

            // Add required/seed SQL Inserts to the baseline
            fullDump += File("$migrationDir/../templates/seed_baseline_db.sql").readText()

            // INSERT baseline versions into flyway history tables
            fullDump += "\n-- Seeds Flyway tables with current baseline version.\n"
            listOf("public.flyway_schema_history_preinitialize", "public.flyway_schema_history_postinitialize")
                .forEach { table ->
                    fullDump += """
                            CREATE TABLE IF NOT EXISTS $table (
                                installed_rank integer NOT NULL PRIMARY KEY,
                                version character varying(50),
                                description character varying(200) NOT NULL,
                                type character varying(20) NOT NULL,
                                script character varying(1000) NOT NULL,
                                checksum integer,
                                installed_by character varying(100) NOT NULL,
                                installed_on timestamp without time zone DEFAULT now() NOT NULL,
                                execution_time integer NOT NULL,
                                success boolean NOT NULL
                            );
                    """.trimIndent()
                    fullDump += "\nINSERT INTO $table (installed_rank, version, description, type, script, " +
                        "installed_by, execution_time, success) VALUES (1, '$baselineVersion', '<< " +
                        "Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', session_user, 0, TRUE) " +
                        "ON CONFLICT (installed_rank) DO NOTHING;\n"
                }

            val createIfNotExistsFunction = """
                -- https://stackoverflow.com/questions/8092086
                -- CREATE ROLE does not support IF NOT EXISTS, so we have to mimic that ourselves.
                CREATE OR REPLACE FUNCTION pg_temp.create_role_if_not_exists(rolname name) RETURNS void LANGUAGE plpgsql AS ${'$'}${'$'}
                BEGIN
                  EXECUTE FORMAT('CREATE ROLE %I', rolname);
                EXCEPTION
                  WHEN duplicate_object THEN
                    RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
                END;
                ${'$'}${'$'};
            """.trimIndent()
            fullDump = createIfNotExistsFunction + fullDump
            fullDump = Regex("CREATE ROLE (seeq|seeq_rw|seeq_ro);")
                .replace(fullDump) { matchResult ->
                    // 0 is the full match, groups index starting at 1
                    "SELECT pg_temp.create_role_if_not_exists('${matchResult.groups.get(1)!!.value}');"
                }

            fullDump = Regex("CREATE DATABASE seeq .*(\\r\\n|\\r|\\n)").replace(fullDump, "")

            // Read baseline template, insert query into template, and write baseline to a file.
            fullDump = File("$migrationDir/../templates/BaselineTemplate.kt").readText()
                .replace(
                    "package com.seeq.appserver.migrations.templates",
                    "package com.seeq.appserver.migrations.preinitialize",
                )
                .replace("<REPLACE WITH QUERY>", fullDump)
            fullDump = fullDump.replace("BaselineTemplate", "B${baselineVersion}__auto_generated_baseline")
            File(outputFile).appendText(fullDump)
            println("Saved baseline to $outputFile")
        }
    }

    // TODO CRAB-40718: Remove ignore once methods are refactored to remove this-escape condition
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:-this-escape",
            ),
        )
    }
}