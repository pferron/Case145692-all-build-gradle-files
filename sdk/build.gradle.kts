import com.seeq.build.SharedStringGeneratorTask
import com.seeq.build.crossPlatformCommandLine
import com.seeq.build.isWindows
import com.seeq.build.swagger.SwaggerCodegen
import com.seeq.build.swagger.SwaggerMerge
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    com.seeq.build.`java-module`
    id("com.github.johnrengelman.shadow")
    com.seeq.build.sdk.`connector-sdk-part`
    idea
    com.seeq.build.revapi.revapi
}

group = "com.seeq"

val seeqVersion: String by project.extra
val seeqVersionNoSuffix: String by project.extra

sourceSets {
    main {
        java {
            srcDir("target/java/src/main/java")
        }
        resources {
            srcDir("target/java/src/main/resources")
        }
    }
}

revapi {
    oldVersion.set("62.0.3-v202308310157")
}

val appserverSwaggerSchema by configurations.creating
val emailerSwaggerSchema by configurations.creating
val mergedSwaggerSchema by configurations.creating
val seeqNames by configurations.creating

dependencies {
    api(platform(project(":seeq-platform")))
    api("org.glassfish.jersey.core:jersey-client")
    api("io.swagger.core.v3:swagger-annotations")
    api("javax.ws.rs:javax.ws.rs-api")
    implementation("org.glassfish.jersey.media:jersey-media-multipart")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson")
    implementation("org.glassfish.jersey.inject:jersey-hk2")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.github.joschi.jackson:jackson-datatype-threetenbp:2.6.4")
    implementation("com.brsanthu:migbase64")

    appserverSwaggerSchema(project(":appserver:appserver-query", "appserverSwaggerSchema"))
    emailerSwaggerSchema(project(":emailer", "emailerSwaggerSchema"))

    // TODO: CRAB-40349 add genai swagger schema once we have public endpoints
    // emailerSwaggerSchema(project(":genai", "genaiSwaggerSchema"))

    seeqNames(project(":seeq-utilities", "seeqNames"))
}

idea {
    module {
        generatedSourceDirs.add(file("target/java/src/main/java"))
        generatedSourceDirs.add(file("target/java/src/main/resources"))
    }
}

tasks {
    clean {
        dependsOn(
            "cleanGeneratePythonConstants",
            "cleanCopySdkJarsToExamples",
            "cleanCopyPythonSdkToPyPI",
            "cleanCopyJavascriptSdkToExamples",
            "cleanCopyPythonEggToExamples",
            "cleanCopyCSharpSdkDllsToExamples",
            "cleanCopyRubyGemToExamples",
            "cleanNuGetInstallSdkPackages",
            "cleanNuGetInstallUtilitiesPackages",
        )
    }

    val mergeSwaggerSchemas by registering(SwaggerMerge::class) {
        mainSwaggerSchema.fileProvider(appserverSwaggerSchema.elements.map { it.single().asFile })
        additionalSchemas.from(emailerSwaggerSchema)
        outputFile.set(layout.buildDirectory.file("swagger-merged.json"))
    }

    artifacts {
        add(mergedSwaggerSchema.name, mergeSwaggerSchemas)
    }

    fun registeringSwaggerCodeGen(language: String, action: SwaggerCodegen.() -> Unit = {}) =
        registering(SwaggerCodegen::class) {
            configFile.set(file("$language-config.json"))
            inputSpec.set(mergeSwaggerSchemas.flatMap { it.outputFile })
            postProcessingPythonScript.set(file("build/add_sdk_overloads.py"))
            templateDir.set(file("swagger-templates/$language"))
            lang.set(language)
            systemProperties.put("modelTests", "false")
            systemProperties.put("apiTests", "false")
            systemProperties.put("hideGenerationTimestamp", "true")
            outputDir.set(layout.buildDirectory.dir(language))
            action(this)
        }

    //region Java SDK
    val swaggerCodegenJava by registeringSwaggerCodeGen("java")

    compileJava {
        dependsOn(swaggerCodegenJava)
        // This compiles generated code, so we'll tolerate warnings.
        options.compilerArgs.remove("-Werror")
    }

    sourcesJar {
        dependsOn(swaggerCodegenJava)
    }

    processResources {
        dependsOn(swaggerCodegenJava)
    }

    shadowJar {
        mergeServiceFiles()
        relocate("com.fasterxml.jackson", "shaded.seeq.com.fasterxml.jackson")
        relocate("org.glassfish", "shaded.seeq.org.glassfish")
        relocate("com.migcomponents", "shaded.seeq.com.migcomponents")
        relocate("javassist", "shaded.seeq.javassist")
        relocate("org.jvnet", "shaded.seeq.org.jvnet")
    }

    val copySdkJarsToExamples by registering(Sync::class) {
        from(shadowJar)
        into("examples/v1/java/src/lib")
    }

    val buildJava by registering {
        dependsOn(copySdkJarsToExamples)
    }
    //endregion

    //region Python SDK
    val pyPiPackage by configurations.creating
    val pyPiPackageDist by configurations.creating
    val pyPiPackageDocs by configurations.creating
    val pyPiPackageSpy by configurations.creating
    val pyPiPackageSpyDist by configurations.creating
    val pyPiDir = file("pypi")

    val swaggerCodegenPython by registeringSwaggerCodeGen("python") {
        val pythonLogLevel = pythonLogLevel
        val setupPy = file("setup.py")
        inputs.file(setupPy).withPathSensitivity(PathSensitivity.NONE).withPropertyName("setup.py")

        val fileSystemOperations = serviceOf<FileSystemOperations>()
        val execOperations = serviceOf<ExecOperations>()
        doLast {
            fileSystemOperations.copy {
                from(setupPy)
                into(outputDir)
            }
            execOperations.exec {
                workingDir(outputDir)
                crossPlatformCommandLine("python setup.py $pythonLogLevel bdist_egg")
            }
        }
    }

    val generatePythonConstants by registering(SharedStringGeneratorTask::class) {
        jsonFile.fileProvider(seeqNames.elements.map { it.single().asFile })
        generate = file("$pyPiDir/seeq/base/seeq_names.py")
    }

    val copyPythonSdkToPyPI by registering(Sync::class) {
        from(swaggerCodegenPython.map { it.outputDir.dir("seeq_sdk") })
        into("$pyPiDir/seeq/sdk")
    }

    val generatePyPiPackage by registering {
        val pyPiDir = pyPiDir
        val pythonLogLevel = pythonLogLevel
        inputs.property("operatingSystem", OperatingSystem.current().familyName)
        inputs.file(File(pyPiDir, "setup.py")).withPathSensitivity(PathSensitivity.NONE)
        inputs.files(
            fileTree(File(pyPiDir, "seeq")) {
                exclude("**/__pycache__/")
                exclude("**/.pytest_cache/")
            }.builtBy(copyPythonSdkToPyPI, generatePythonConstants),
        )
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .ignoreEmptyDirectories()
        outputs.dir(File(pyPiDir, "build"))
        outputs.dir(File(pyPiDir, "dist"))
        outputs.dir(File(pyPiDir, "seeq.egg-info"))
        outputs.cacheIf { true }
        val fileSystemOperations = serviceOf<FileSystemOperations>()
        val execOperations = serviceOf<ExecOperations>()
        doFirst {
            fileSystemOperations.delete {
                delete(
                    "$pyPiDir/dist", "$pyPiDir/build", "$pyPiDir/seeq.egg-info",
                    "$pyPiDir/seeq/spy/docs/Documentation/.ipynb_checkpoints",
                )
            }
            execOperations.exec {
                // use keep-temp=true for a wheel to fix a flaky bug with remove a temporary directory. See CRAB-26181
                crossPlatformCommandLine("python setup.py $pythonLogLevel sdist bdist_wheel --keep-temp")
                workingDir(pyPiDir)
            }
        }
    }

    val copySeeqPyPItoSeeqSpyPyPI by registering(Sync::class) {
        dependsOn(generatePyPiPackage)
        from("$pyPiDir/seeq")
        into("$pyPiDir/seeq-spy/seeq")
    }

    val generatePyPiSpyPackage by registering {
        val spyDir = File(pyPiDir, "seeq-spy")
        val pythonLogLevel = pythonLogLevel
        inputs.property("operatingSystem", OperatingSystem.current().familyName)
        inputs.file(File(spyDir, "setup.py")).withPathSensitivity(PathSensitivity.NONE)
        inputs.files(
            fileTree(File(spyDir, "seeq")) {
                exclude("**/__pycache__/")
                exclude("**/.pytest_cache/")
            }.builtBy(copySeeqPyPItoSeeqSpyPyPI),
        ).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        outputs.dir(File(spyDir, "build"))
        outputs.dir(File(spyDir, "dist"))
        outputs.dir(File(spyDir, "seeq.egg-info"))
        outputs.cacheIf { true }
        val fileSystemOperations = serviceOf<FileSystemOperations>()
        val execOperations = serviceOf<ExecOperations>()
        doFirst {
            fileSystemOperations.delete {
                delete(
                    "$spyDir/dist", "$spyDir/build", "$spyDir/seeq.egg-info",
                    "$spyDir/seeq/spy/docs/Documentation/.ipynb_checkpoints",
                )
            }
            execOperations.exec {
                // use keep-temp=true for a wheel to fix a flaky bug with remove a temporary directory. See CRAB-26181
                crossPlatformCommandLine("python setup.py $pythonLogLevel sdist bdist_wheel --keep-temp")
                workingDir(spyDir)
            }
        }
    }

    artifacts {
        add(pyPiPackage.name, File(pyPiDir, "build/lib/seeq")) {
            builtBy(generatePyPiPackage)
        }
        add(pyPiPackageDist.name, File(pyPiDir, "dist")) {
            builtBy(generatePyPiPackage)
        }
        add(pyPiPackageDocs.name, File(pyPiDir, "seeq/spy/docs/Documentation")) {
            builtBy(generatePyPiPackage)
        }
        add(pyPiPackageSpy.name, File(pyPiDir, "seeq-spy/build/lib/seeq")) {
            builtBy(generatePyPiSpyPackage)
        }
        add(pyPiPackageSpyDist.name, File(pyPiDir, "seeq-spy/dist")) {
            builtBy(generatePyPiSpyPackage)
        }
    }

    val testPyPIPackage by registering {
        inputs.property("operatingSystem", OperatingSystem.current().familyName)
        dependsOn(generatePyPiPackage)
        inputs.files(
            fileTree("$pyPiDir/seeq") {
                exclude("**/__pycache__/")
                exclude("**/.pytest_cache/")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        val reportFile = File(buildDir, "test-reports/testPyPIPackage.xml")
        outputs.file(reportFile)
        outputs.cacheIf { true }
        val exec = serviceOf<ExecOperations>()
        val workingDir = "$pyPiDir/seeq"
        doLast {
            exec.exec {
                crossPlatformCommandLine("pytest -v -m unit -n auto --timeout 3600 --junitxml=$reportFile")
                workingDir(workingDir)
            }
        }
    }

    check {
        dependsOn(testPyPIPackage)
    }

    val copyPythonEggToExamples by registering(Sync::class) {
        from(swaggerCodegenPython.map { it.outputDir.dir("dist") })
        into("examples/v1/python/src/lib")
    }

    val buildPython by registering {
        dependsOn(generatePyPiPackage, copyPythonEggToExamples)
    }
    //endregion

    //region Javascript SDK
    val javascriptSdk by configurations.creating

    val swaggerCodegenJavascript by registeringSwaggerCodeGen("javascript")

    val copyJavascriptSdkToExamples by registering(Sync::class) {
        from(swaggerCodegenJavascript)
        into("examples/v1/javascript")
    }

    val buildJavascript by registering {
        dependsOn(copyJavascriptSdkToExamples)
    }

    artifacts {
        add(javascriptSdk.name, swaggerCodegenJavascript)
    }
    //endregion

    //region Frontend SDK
    val swaggerCodegenTypescript by registeringSwaggerCodeGen("typescript-react")

    val buildFrontend by registering {
        dependsOn(swaggerCodegenTypescript)
    }
    //endregion

    //region Ruby SDK
    val swaggerCodegenRuby by registeringSwaggerCodeGen("ruby") {
        val exec = serviceOf<ExecOperations>()
        doLast {
            exec.exec {
                crossPlatformCommandLine("gem build seeq_sdk.gemspec")
                workingDir(outputDir)
            }
        }
    }

    val copyRubyGemToExamples by registering(Sync::class) {
        from(swaggerCodegenRuby.map { it.outputDir.file("seeq_sdk-$seeqVersionNoSuffix.gem") })
        into("examples/v1/ruby/src/lib")
    }

    val buildRuby by registering {
        dependsOn(copyRubyGemToExamples)
    }
    //endregion

    //region MATLAB SDK
    val buildMatlab by registering(Sync::class) {
        from("matlab")
        into("$buildDir/matlab")
    }
    //endregion

    //region C# SDK

    val nuGetInstallSdkPackages by registering {
        val inputFile = file("packages.config")
        val outputDir = file("packages")
        inputs.file(inputFile).withPathSensitivity(PathSensitivity.RELATIVE)
        outputs.dir(outputDir)
        outputs.cacheIf { true }
        val exec = serviceOf<ExecOperations>()
        doLast {
            outputDir.deleteRecursively()
            exec.exec {
                crossPlatformCommandLine("nuget install \"$inputFile\" -OutputDirectory \"$outputDir\"")
            }
        }
    }

    val nuGetInstallUtilitiesPackages by registering {
        val inputFile = file("../utilities/csharp/packages.config")
        val outputDir = file("../utilities/csharp/packages")
        inputs.file(inputFile).withPathSensitivity(PathSensitivity.RELATIVE)
        outputs.dir(outputDir)
        outputs.cacheIf { true }
        val exec = serviceOf<ExecOperations>()
        doLast {
            outputDir.deleteRecursively()
            exec.exec {
                crossPlatformCommandLine("nuget install \"$inputFile\" -OutputDirectory \"$outputDir\"")
            }
        }
    }

    val swaggerCodegenCSharp by registeringSwaggerCodeGen("csharp") {
        val sdkPackages = fileTree("packages") {
            include("**/net452/RestSharp.dll", "**/net40/Newtonsoft.Json.dll", "**/net45/JsonSubTypes.dll")
            builtBy(nuGetInstallSdkPackages)
        }
        val compileBat = file("compile.bat")
        val assemblyInfo = file("AssemblyInfo.cs")
        inputs.files(sdkPackages).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        inputs.file(compileBat).withPathSensitivity(PathSensitivity.NONE)
        inputs.file(assemblyInfo).withPathSensitivity(PathSensitivity.NAME_ONLY)

        val exec = serviceOf<ExecOperations>()
        val fs = serviceOf<FileSystemOperations>()
        doLast {
            fs.copy {
                from(sdkPackages)
                into(outputDir.dir("bin"))
                eachFile {
                    path = sourceName
                }
                includeEmptyDirs = false
                duplicatesStrategy = DuplicatesStrategy.FAIL
            }
            fs.copy {
                from(compileBat)
                into(outputDir)
            }
            fs.copy {
                from(assemblyInfo)
                into(outputDir.dir("src"))
            }
            exec.exec {
                crossPlatformCommandLine("compile.bat")
                workingDir(outputDir)
            }
        }
    }

    val copyCSharpSdkDllsToExamples by registering(Sync::class) {
        from(swaggerCodegenCSharp.map { it.outputDir.dir("bin") })
        into("examples/v1/csharp/src/lib")
    }

    val copyCSharpSdkDllsForNuget by registering(Sync::class) {
        from(swaggerCodegenCSharp.map { it.outputDir.dir("bin") })
        into("target/csharp/src/Seeq.Sdk/bin/Release")
    }

    val buildCsharp by registering {
        dependsOn(nuGetInstallUtilitiesPackages, copyCSharpSdkDllsToExamples, copyCSharpSdkDllsForNuget)
    }

    //endregion

    assemble {
        dependsOn(
            buildJava,
            buildPython,
            buildJavascript,
            buildFrontend,
            buildMatlab,
        )
        if (isWindows) {
            dependsOn(
                buildCsharp,
                buildRuby,
            )
        }
    }
}

val Task.pythonLogLevel by lazy {
    when {
        logger.isDebugEnabled -> "-v"
        logger.isInfoEnabled -> ""
        else -> "-q"
    }
}