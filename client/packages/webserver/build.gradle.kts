import com.seeq.build.SharedStringGeneratorTask
import com.seeq.build.crossPlatformCommandLine
import com.seeq.build.npm.Jest
import com.seeq.build.npm.NodeTask
import com.seeq.build.npm.TsNode
import com.seeq.build.npm.Tsc
import com.seeq.build.npm.Vite
import org.gradle.configurationcache.extensions.serviceOf

plugins {
  com.seeq.build.npm.`node-module`
  com.seeq.build.docker.docker
}

val seeqNames by configurations.creating
val dist by configurations.creating
val mergedSwaggerSchema by configurations.creating

dependencies {
  seeqNames(project(":seeq-utilities", "seeqNames"))
  mergedSwaggerSchema(project(":seeq-sdk", "mergedSwaggerSchema"))
}

tasks {
  val copyClientSdk by registering(Copy::class) {
    dependsOn(":seeq-sdk:buildFrontend")
    val fs = serviceOf<FileSystemOperations>()
    doFirst {
      fs.delete {
        delete("app/src/sdk")
      }
    }
    from("../../../sdk/target/typescript-react")
    into("app/src/sdk")
    doLast {
      fs.delete {
        delete("app/src/sdk/api.module.ts", "app/src/sdk/api/api.ts")
      }
    }
  }

  val copySwaggerSchema by registering {
    val inputFiles = files(mergedSwaggerSchema)
    val outputFile = file("public/api-docs/swagger.json")
    inputs.files(inputFiles).withPathSensitivity(PathSensitivity.NAME_ONLY)
    outputs.file(outputFile)
    doLast {
      // This allows the file to be served statically by nginx. It can't be done by the merge schema task because this
      // URL is set as the default baseUrl in the SDK and it would break backwards compatibility to change it.
      outputFile.writeText(inputFiles.singleFile.readText().replace("http://localhost:34218/api", "/api"))
    }
  }

  val generateTsConstants by registering(SharedStringGeneratorTask::class) {
    jsonFile.fileProvider(seeqNames.elements.map { it.single().asFile })
    generate = file("app/src/main/app.constants.seeqnames.ts")
  }

  val prepareBuild by registering {
    description = "Installs 3rd party dependencies and copies artifacts from other seeq sub-projects"
    dependsOn(copyClientSdk)
    dependsOn(copySwaggerSchema)
    dependsOn(generateTsConstants)
    dependsOn(npmInstall)
  }

  named("cleanPrepareBuild") {
    dependsOn("cleanCopyClientSdk")
    dependsOn("cleanCopySwaggerSchema")
    dependsOn("cleanGenerateTsConstants")
    dependsOn("cleanNpmInstall")
  }

  val scripts = fileTree("scripts")
  val appSources =
    fileTree("app") {
      include("**/*.js")
      include("**/*.ts")
      include("**/*.tsx")
      include("**/*.html")
      include("**/*.scss")
      include("**/*.ttf")
      include("**/*.otf")
      include("**/*.eot")
      include("**/*.svg")
      include("**/*.woff")
      include("**/*.woff2")
      include("**/*.json")
      include("**/*.png")
      include("**/*.jpg")
      include("**/*.gif")
      include("**/*.ico")
      include("**/*.webp")
      include("**/*.css")
      exclude("**/seeq.js")
      exclude("**/seeq.d.ts")
      exclude("**/*generated*")

      builtBy(prepareBuild)
    }.plus(files("tsconfig.json", "babel.config.js", "vite.config.js"))

  val publicAssets = fileTree("public")

  val generatePluginApi by registering(TsNode::class) {
    description = "Builds the frontend plugin api and types"
    inputs.files(appSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(fileTree("plugin/api")).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()

    outputs.file("app/src/plugin/generatedPluginApi.ts")
    outputs.file("plugin/sdk/seeq.d.ts")
    outputs.cacheIf { true }

    args.add("plugin/api/pluginApiGenerator.ts")
    args.addAll("--definitionFile", "app/src/plugin/pluginApiDefinition.ts")
    args.addAll("--templateFile", "plugin/api/pluginApiTemplate.ts")
    args.addAll("--generatedApiFile", "app/src/plugin/generatedPluginApi.ts")
    args.addAll("--generatedTypedefFile", "plugin/sdk/seeq.d.ts")
  }

  val generateTestPluginApi by registering(TsNode::class) {
    description = "Builds a test frontend plugin api and types"

    inputs.files(appSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(fileTree("plugin/api")).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.file("plugin/sdk/seeq.js").withPathSensitivity(PathSensitivity.RELATIVE)

    outputs.file("app/src/plugin/test/it/plugin-api/generatedTestPluginApi.ts")
    outputs.file("app/src/plugin/test/it/serve/test-plugins/test-sdk/seeq.js")
    outputs.file("app/src/plugin/test/it/serve/test-plugins/test-sdk/seeq.d.ts")
    outputs.cacheIf { true }

    val fs = serviceOf<FileSystemOperations>()

    doFirst {
      fs.copy {
        from("plugin/sdk/seeq.js")
        into("app/src/plugin/test/it/serve/test-plugins/test-sdk")
      }
    }

    args.add("plugin/api/pluginApiGenerator.ts")
    args.addAll("--definitionFile", "app/src/plugin/test/it/plugin-api/testPluginApiDefinition.ts")
    args.addAll("--templateFile", "plugin/api/pluginApiTemplate.ts")
    args.addAll("--generatedApiFile", "app/src/plugin/test/it/plugin-api/generatedTestPluginApi.ts")
    args.addAll("--generatedTypedefFile", "app/src/plugin/test/it/serve/test-plugins/test-sdk/seeq.d.ts")
  }

  val plugin = files("plugin", generatePluginApi, generateTestPluginApi)

  withType<NodeTask>().configureEach {
    nodeArgs.add("--max-old-space-size=2000")
  }

  val tsc by registering(Tsc::class) {
    description = "Checks the project for typescript errors"

    sources.from(appSources)
    sources.from(plugin)
  }

  val vite by registering(Vite::class) {
    description = "Uses Vite to bundle the client side code"

    sources.from(appSources)
    sources.from(publicAssets)
    sources.from(plugin)
    sources.from(scripts)

    distributions.from("dist")
  }

  artifacts {
    add(dist.name, vite)
  }

  val testTranslations by registering {
    inputs.dir("build/tests").withPathSensitivity(PathSensitivity.RELATIVE)
    val exec = serviceOf<ExecOperations>()
    doLast {
      exec.exec {
        crossPlatformCommandLine("pytest -v -m unit")
        workingDir("build")
      }
    }
  }

  withType<Jest> {
    roots.add("app/src")

    inputs.files(appSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(plugin).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(scripts).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    /**
     * High concurrency can give big speedups, especially if webserver:test is the only task the build needs to run,
     * but can also lead to timeouts when the build machine is also utilizing cores for build processes (e.g. Clean builds).
     */
    maxWorkers.set("50%")
    jestTimeout.set("60000")
  }

  // test coverage has proved to be costly given the way that we implement jest testing. Once all index
  // files are removed we may be able to consider re-enabling coverage. This extra task can be used to
  // manually collect coverage with `./gradlew :client:package-webserver:testCoverage`. The coverage
  // report is found in `webserver/coverage-jest/testCoverage/lcov-report/index.html` after the run is
  // complete.
  val testCoverage by registering(Jest::class) {
    description = "Runs jest unit tests with coverage"
    testMatchers.addAll(".test.ts", ".test.tsx")
    args.add("--coverage") // Coverage limits are set in webserver/jest.config.js
  }

  dependencyCruiser {
    sources.from("app/src")
    inputs.files(appSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(plugin).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
  }

  clean {
    dependsOn("cleanPrepareBuild")
    dependsOn("cleanGeneratePluginApi")
    dependsOn("cleanGenerateTestPluginApi")
    dependsOn("cleanTsc")
  }

  assemble {
    dependsOn(generatePluginApi)
    dependsOn(tsc)
    dependsOn(vite)
  }

  check {
    dependsOn(testTranslations)
  }

  dockerBuild {
    imageName.set("webserver-dev")
  }
}
