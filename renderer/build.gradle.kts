import com.seeq.build.SharedStringGeneratorTask
import com.seeq.build.ToolchainDownload
import com.seeq.build.npm.Jest

plugins {
  com.seeq.build.npm.`node-module`
  com.seeq.build.docker.docker
}

val chromiumVersion = "117.0.5938.88"

val seeqNames by configurations.creating

dependencies {
  seeqNames(project(":seeq-utilities", "seeqNames"))
}

tasks {
  val prepareBuild by registering {
    description = "Installs 3rd party dependencies and copies artifacts from other seeq sub-projects"
    dependsOn("copyServerSdk")
    dependsOn("generateJsConstants")
    dependsOn("npmInstall")
  }

  val serverSources =
    fileTree("server") {
      include("**/*.js")
      include("**/*.json")
      include("**/*.png")
      include("**/*.jpg")

      builtBy(prepareBuild)
    }

  register<Copy>("copyServerSdk") {
    dependsOn(":seeq-sdk:buildJavascript")
    val targetDir = file("server/sdk/src")
    doFirst {
      targetDir.deleteRecursively()
    }
    from("../sdk/target/javascript/src")
    into(targetDir)
  }

  register<SharedStringGeneratorTask>("generateJsConstants") {
    jsonFile.fileProvider(seeqNames.elements.map { it.single().asFile })
    generate = file("server/constants.seeqnames.js")
  }
  withType<Jest> {
    roots.add("server")

    inputs.files(serverSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
  }

  val downloadChromium by registering(ToolchainDownload::class) {
    filename.set("chromium-$chromiumVersion-linux.tar.gz")
  }

  dockerBuildContext {
    from(serverSources) {
      exclude("**/test/**")
      into("server")
    }
    from("$projectDir/package.json")
    from("$projectDir/package-lock.json")
    from(tarTree(downloadChromium.map { it.outputs.files.singleFile })) {
      into("chromium")
    }
  }

  dockerBuild {
    buildArgs.put("CHROMIUM_REVISION", chromiumVersion)
  }

  named("cleanPrepareBuild") {
    dependsOn("cleanCopyServerSdk")
    dependsOn("cleanGenerateJsConstants")
    dependsOn("cleanNpmInstall")
  }

  test {
    args.add("--coverage") // Coverage limits are set in renderer/jest.config.js
  }

  clean {
    dependsOn("cleanPrepareBuild")
  }

  assemble {
    dependsOn("prepareBuild")
  }
}
