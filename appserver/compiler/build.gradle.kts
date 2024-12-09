plugins {
    com.seeq.build.appserver.`appserver-module`
    antlr
}

dependencies {
    api(project(":seriesdata"))
    api(project(":cache:cache-quantity-interfaces"))
    api(project(":cache:cache-quantity-regression-model"))
    api(project(":cache:cache-quantity-onnx-model"))
    api(project(":cache:cache-quantity-reference-slice"))
    api(project(":cache:cache-quantity-serializable-data"))
    api(project(":cache:cache-quantity-scalar"))
    api(project(":cache:cache-quantity-table"))
    api(project(":compute:data"))
    api(project(":compute:compute-serialization-onnx"))

    implementation(project(":cache:cache-pipeline-series"))
    implementation(project(":seeq:common-concurrent"))
    implementation(project(":seeq:common-activity"))
    implementation(project(":seeq:common-seeq-monitors"))
    implementation("org.reflections:reflections")
    implementation("org.antlr:antlr4-runtime:4.7.1")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.hummeling:if97:1.0.5")
    implementation("io.github.classgraph:classgraph:4.8.177")

    testImplementation(testFixtures(project(":compute:data")))
    testImplementation(project(":appserver:appserver-analytics"))
    testImplementation(testFixtures(project(":appserver:appserver-analytics")))

    testFixturesApi(testFixtures(project(":seriesdata")))
    testFixturesApi(testFixtures(project(":compute:data")))
    testFixturesApi("com.googlecode.junit-toolbox:junit-toolbox:2.4")
    testFixturesImplementation(testFixtures(project(":seeq:common-monitoring")))
    testFixturesImplementation(project(":cache:cache-quantity-table"))
    testFixturesImplementation("org.reflections:reflections")

    antlr("org.antlr:antlr4:4.7.1")
}

coverage {
    // TODO CRAB-45077 raise this back up to 0.64 when the updated operators use the new symbolic features
    threshold.set(0.63)
    excludes.addAll(
        "**/analytics/dsl/generated/**/*.class",
        "com/seeq/appserver/analytics/analysis",
    )
}

tasks {
    generateGrammarSource {
        arguments = arguments + listOf("-visitor", "-listener")
        arguments = arguments + listOf("-encoding", "UTF-8")

        doFirst {
            println("Copying generated grammar lexer/parser files to main directory.")
        }
    }

    compileKotlin {
        dependsOn(generateGrammarSource)
    }

    compileTestFixturesKotlin {
        dependsOn(generateTestFixturesGrammarSource)
    }

    compileTestKotlin {
        dependsOn(generateTestGrammarSource)
    }

    // TODO CRAB-40718: antlr has an open pull request to ignore this warning internally: https://github.com/antlr/antlr4/pull/4486
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:-this-escape",
            ),
        )
    }
}