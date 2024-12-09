import com.seeq.build.performance.Jmh
import org.gradle.configurationcache.extensions.serviceOf

plugins {
    com.seeq.build.appserver.`appserver-module`
    com.seeq.build.performance.jmh
    com.seeq.build.protobuf
}

description = "appserver-analytics"

dependencies {
    api(project(":appserver:appserver-compiler"))
    api(project(":seriesdata"))
    api(project(":compute:compute-serialization-onnx"))
    implementation(project(":cache:cache-quantity-table"))

    implementation("org.reflections:reflections")
    implementation("com.opencsv:opencsv")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.microsoft.onnxruntime:onnxruntime:1.18.0")

    // Used in regression
    implementation("com.github.haifengl:smile-core:1.5.1")
    implementation("com.hummeling:if97:1.0.5")

    jmhImplementation(project(":seeq:common-serialization-seriesdata"))

    testImplementation(testFixtures(project(":compute:data")))

    testFixturesApi(testFixtures(project(":appserver:appserver-compiler")))
    testFixturesApi(testFixtures(project(":compute:compute-formula-support-formuladocs")))
    testFixturesApi(testFixtures(project(":seriesdata")))
    testFixturesApi(testFixtures(project(":compute:data")))
    testFixturesApi("com.googlecode.junit-toolbox:junit-toolbox:2.4")
    testFixturesImplementation(project(":seeq:common-serialization-seriesdata"))
    testFixturesImplementation("org.reflections:reflections")
    testFixturesImplementation("com.opencsv:opencsv")
}

coverage {
    threshold.set(0.82)
}

val generatedBenchmarksFolder = file("$buildDir/generated/source/operator/jmh")

sourceSets {
    jmh {
        java {
            srcDir(generatedBenchmarksFolder)
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedBenchmarksFolder)
    }
}

tasks {
    val generateOperatorBenchmarks by creating {
        val inputDir = file("src/test/resources/perf")
        inputs.files(inputDir).withPathSensitivity(PathSensitivity.NONE).ignoreEmptyDirectories()
        val runtimeClasspath = sourceSets.testFixtures.get().runtimeClasspath
        inputs.files(runtimeClasspath).withNormalizer(ClasspathNormalizer::class)
        outputs.cacheIf { true }
        val outputDir = generatedBenchmarksFolder
        outputs.dir(outputDir)
        val execOperations = serviceOf<ExecOperations>()
        doFirst {
            execOperations.javaexec {
                classpath = runtimeClasspath
                mainClass.set("com.seeq.appserver.analytics.perf.BenchmarkGeneratorKt")
                args(inputDir, outputDir)
            }
        }
    }

    compileJmhKotlin {
        dependsOn(generateOperatorBenchmarks)
    }

    microBenchmarks {
        excludes.add(".*OperatorBenchmark.*")
        excludes.add(".*CompilerPerformanceBenchmark.*")
    }

    /**
     * The operator benchmarks take >2h, so we can't run them on every commit.
     * They get their own task and TeamCity build, which is run once a day.
     */
    register<Jmh>("operatorBenchmarks") {
        includes.add(".*OperatorBenchmark.*")
        includes.add(".*CompilerPerformanceBenchmark.*")
        forks.set(3)
        warmupIterations.set(30)
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