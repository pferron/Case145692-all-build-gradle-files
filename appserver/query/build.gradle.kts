import com.expediagroup.graphql.plugin.gradle.config.GraphQLScalar
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateSDLTask
import com.expediagroup.graphql.plugin.gradle.tasks.GraphQLGenerateTestClientTask
import com.seeq.build.swagger.ExtractSwaggerSchema

plugins {
    com.seeq.build.appserver.`appserver-module`
    id("com.expediagroup.graphql")
}

description = "appserver-query"

val seeqVersion: String by project

dependencies {
    api(project(":seriesdata"))
    api("javax.ws.rs:javax.ws.rs-api")

    implementation(project(":appserver:appserver-corelib"))
    implementation(project(":appserver:appserver-items"))
    implementation(project(":appserver:appserver-drivers-graph"))
    implementation(project(":appserver:appserver-jobs"))
    implementation(project(":appserver:appserver-messaging"))
    implementation(project(":appserver:appserver-reports"))
    implementation(project(":appserver:appserver-compute"))
    implementation(project(":appserver:appserver-tables"))
    implementation(project(":compute:compute-formula-support-contract"))
    implementation(project(":seeq:common-json"))
    implementation(project(":seeq:common-jira"))
    implementation(project(":cache:cache-persistent-client"))?.because("Cache Debug API")
    implementation(project(":seeq:common-serialization-seriesdata"))?.because("Cache Debug API")
    implementation(project(":datasource-proxy:datasource-proxy-client"))?.because("ConnectionCommandRouter")

    implementation("io.swagger.core.v3:swagger-annotations:2.1.10")
    implementation("com.cronutils:cron-utils")
    implementation("it.burning:cron-expression-descriptor")
    implementation("org.apache.poi:poi-ooxml")
    implementation("com.github.ua-parser:uap-java:1.5.0")
    implementation("org.apache.tika:tika-core:1.28")
    implementation("org.apache.olingo:olingo-odata2-jpa-processor-api:2.0.11")
    implementation("org.apache.olingo:olingo-odata2-jpa-processor-core:2.0.11")
    implementation("org.apache.olingo:olingo-odata2-api-annotation:2.0.11")
    implementation("commons-validator:commons-validator")
    implementation("com.expediagroup:graphql-kotlin-server")
    implementation("com.expediagroup:graphql-kotlin-hooks-provider")
    implementation("com.graphql-java:graphql-java-extended-scalars:20.2")
    implementation("com.microsoft.azure:msal4j:1.13.10")
    compileOnlyApi(project(":seeq:common-autofactory-ksp"))
    ksp(project(":seeq:common-autofactory-ksp"))

    testImplementation(project(":cache:cache-pipeline-series"))
    testImplementation(project(":cache:cache-quantity-scalar"))
    testImplementation(project(":cache:cache-quantity-content"))
    testImplementation(project(":cache:cache-persistent-contract"))
    testImplementation(testFixtures(project(":appserver:appserver-server")))
    testImplementation(testFixtures(project(":appserver:appserver-compute")))
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation("org.jboss.resteasy:resteasy-multipart-provider:3.1.2.Final")
    testImplementation("com.expediagroup:graphql-kotlin-client")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("org.awaitility:awaitility")

    testFixturesImplementation("io.swagger.core.v3:swagger-annotations:2.1.10")
    testFixturesApi("io.swagger:swagger-jersey2-jaxrs:1.5.16")
    testFixturesApi("org.jboss.resteasy:resteasy-client")
    testFixturesApi("com.expediagroup:graphql-kotlin-client")
    testFixturesApi(project(":seeq-sdk"))
    testFixturesApi(testFixtures(project(":appserver:appserver-datalayer")))
    testFixturesApi(testFixtures(project(":appserver:appserver-corelib")))
    testFixturesApi(testFixtures(project(":appserver:appserver-items")))
    testFixturesApi(testFixtures(project(":appserver:appserver-analytics")))
    testFixturesApi(testFixtures(project(":compute:compute-engine-contract")))
    testFixturesApi(testFixtures(project(":seriesdata")))
}

val appserverSwaggerSchema by configurations.creating

val graphqlGenerateSDL by tasks.existing(GraphQLGenerateSDLTask::class) {
    packages.set(listOf("com.seeq.appserver.query.v1.graphql.queries"))
}

val graphqlGenerateTestClient by tasks.existing(GraphQLGenerateTestClientTask::class) {
    packageName.set("com.seeq.graphql.generated")
    schemaFile.set(graphqlGenerateSDL.map { it.schemaFile }.get())
    queryFileDirectory.set(projectDir.resolve("src/test/resources/graphqlQueries"))
    customScalars.add(
        GraphQLScalar(
            "Object", "kotlin.Any",
            "com.seeq.appserver.query.graphql.ObjectConverter",
        ),
    )
    customScalars.add(
        GraphQLScalar(
            "Value", "kotlin.Any",
            "com.seeq.appserver.query.graphql.ObjectConverter",
        ),
    )
}

tasks {
    afterEvaluate {
        named("kspTestKotlin") {
            dependsOn(graphqlGenerateTestClient)
        }
        // The GraphQL plugin doesn't have an option for specifying the output directory source type (i.e. test vs
        // testFixtures) so we need to do this. Filed https://github.com/ExpediaGroup/graphql-kotlin/issues/1935 to
        // fix that
        sourceSets.testFixtures.get().java.srcDir(graphqlGenerateTestClient.get().outputDirectory)
        sourceSets.test.get().java.setSrcDirs(listOf("src/test/java"))
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

val extractSwaggerSchema by tasks.registering(ExtractSwaggerSchema::class) {
    classpath.from(
        tasks.compileJava.get().destinationDirectory,
        tasks.compileKotlin.get().destinationDirectory,
        configurations.runtimeClasspath,
    )
    scannedPackages.add("com.seeq.appserver.query.v1")
    basePath.set("/api")
    host.set("localhost:34218")
    title.set("Seeq REST API")
    version.set(seeqVersion)
    outputDir.set(layout.buildDirectory.dir("swagger-specs"))
    fileBaseName.set("swagger")
}

sourceSets {
    main {
        output.dir(extractSwaggerSchema.map { it.outputDir })
    }
}

artifacts {
    add(appserverSwaggerSchema.name, extractSwaggerSchema)
}

coverage {
    threshold.set(0.75)
    excludes.addAll("**/*Container.class")
}