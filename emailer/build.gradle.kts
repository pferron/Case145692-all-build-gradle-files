import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
    id("com.seeq.build.kotlin-module")
    com.seeq.build.docker.`docker-application`
    id("io.micronaut.minimal.application")
}

val emailerSwaggerSchema by configurations.creating
// Defined in openapi.properties
val emailSwaggerSchemaFile = file("$buildDir/emailer-service.yml")

micronaut {
    version("4.2.0")
    runtime("netty")
    testRuntime("junit5")
    processing {
        annotations("com.seeq.emailer.service.*")
    }
}

application {
    mainClass.set("com.seeq.emailer.service.server.MainKt")
}

// Disabled temporarily, since micronaut-openapi doesn't work incrementally
project.extraProperties.set("ksp.incremental", false)

dependencies {
    api(platform(project(":seeq-platform")))

    ksp("io.micronaut.validation:micronaut-validation")
    ksp("io.micronaut.openapi:micronaut-openapi")

    implementation("javax.inject:javax.inject")

    implementation("ch.qos.logback:logback-core:1.4.14") { 
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }
    implementation("ch.qos.logback:logback-classic:1.4.14") {
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }
    implementation("io.github.microutils:kotlin-logging")
    implementation(project(":seeq-utilities")) {
        because("SeeqNames")
    }
    implementation("io.micronaut:micronaut-http-server-netty") {
        because("The emailer has its own REST Api")
    }
    implementation("io.micronaut.email:micronaut-email") {
        because("The endpoint uses standard micronaut-email interface")
    }
    implementation("com.sendgrid:sendgrid-java:4.8.2")
    implementation("io.micronaut.validation:micronaut-validation") {
        because("Email inputs are validated using micronaut validation")
    }

    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("io.micronaut:micronaut-management") {
        because("emailer health endpoint")
    }
    implementation("software.amazon.awssdk:secretsmanager:2.17.136") {
        because("sendgrid key can be stored in AWS Secrets manager")
    }
    implementation("com.azure:azure-security-keyvault-secrets:4.5.1") {
        because("credentials to access AWS secrets manager are set in KeyVault")
    }
    implementation("com.azure:azure-identity:1.6.1") {
        because("DefaultAzureCredentialBuilder needs it")
    }
    implementation("io.micronaut:micronaut-jackson-databind")

    testImplementation("org.mockito.kotlin:mockito-kotlin")
    testImplementation("io.kotest:kotest-assertions-core-jvm")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("org.assertj:assertj-core")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.yaml:snakeyaml")
}

tasks {
    afterEvaluate {
        tasks.named("kspKotlin") {
            inputs.file("$buildDir/../openapi.properties").withPathSensitivity(RELATIVE)
            outputs.file(emailSwaggerSchemaFile)
        }
    }
}

artifacts {
    add(emailerSwaggerSchema.name, emailSwaggerSchemaFile) {
        builtBy("kspKotlin")
    }
}
