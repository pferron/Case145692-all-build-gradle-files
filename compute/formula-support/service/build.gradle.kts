plugins {
    com.seeq.build.compute.`compute-service-module`
    com.seeq.build.docker.compose
    com.seeq.build.docker.`docker-application`
    com.seeq.build.helm.chart
    id("org.jetbrains.kotlin.plugin.allopen")
    id("io.micronaut.minimal.application")
}

dependencies {
    implementation(project(":compute:compute-formula-support-contract"))
    implementation(project(":compute:compute-formula-support-formuladocs"))
    implementation(project(":seeq:common-grpc-health"))
    implementation(project(":seeq:common-grpc-unhandled-exception"))

    implementation("ch.qos.logback:logback-core:1.4.14") { 
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }
    implementation("ch.qos.logback:logback-classic:1.4.14") {
        because("Micronaut brings in 1.4.11, which has a vulnerability (CRAB-43814). When we do CRAB-39703, the version will be managed in seeq-platform and this can go away.")
    }

    runtimeOnly("org.yaml:snakeyaml")
    runtimeOnly("io.grpc:grpc-netty-shaded")

    testImplementation(project(":seeq-utilities"))
    testImplementation("io.grpc:grpc-testing")

    testFixturesApi("junit:junit")
    testFixturesApi("org.assertj:assertj-core")
    testFixturesApi(kotlin("test-junit"))
}

application {
    mainClass.set("com.seeq.compute.formulasupport.MainKt")
}

micronaut {
    version("4.2.0")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.seeq.compute.formulasupport.*")
    }
}

tasks {
    dockerBuild {
        imageName.set("formula-support")
    }
}
