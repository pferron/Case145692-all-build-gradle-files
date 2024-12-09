import com.gradle.enterprise.gradleplugin.test.ImportJUnitXmlReports
import com.gradle.enterprise.gradleplugin.test.JUnitXmlDialect
import com.seeq.build.behavior.PlaywrightSystemTest
import com.seeq.build.isLinux
import com.seeq.build.npm.Jest
import com.seeq.build.npm.Vite

plugins {
  com.seeq.build.npm.`node-module`
}

tasks {
  val clientSources = fileTree("src").plus(file("index.html"))
  val publicAssets = fileTree("public")
  val configurationFiles =
    fileTree(".") {
      include(".eslintrc.cjs")
      include("tsconfig.json")
      include("tsconfig.node.json")
      include("vite.config.ts")
      include("tailwind.config.js")
      include("postcss.config.js")
      include("vite.config.ts")
    }

  val vite by registering(Vite::class) {
    description = "Uses Vite to bundle the client side code"

    sources.from(clientSources)
    sources.from(publicAssets)
    sources.from(configurationFiles)

    distributions.from("dist")
  }

  withType<Jest> {
    roots.add("src")
    jestConfig.set(file("jest.config.ts"))
    inputs.file("babel.config.cjs").withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file("tsconfig.json").withPathSensitivity(PathSensitivity.NAME_ONLY)
    inputs.files(clientSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(configurationFiles).withPathSensitivity(PathSensitivity.RELATIVE)
  }

  val visualTests by registering(PlaywrightSystemTest::class) {
    enabled = isLinux

    inputs.files(clientSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(publicAssets).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    inputs.files(configurationFiles).withPathSensitivity(PathSensitivity.RELATIVE)

    group = "verification"

    playwrightConfig.set(layout.projectDirectory.file("playwright.config.ts"))
    testReports.set(layout.projectDirectory.dir("test-reports/visual"))
  }

  val includeVisualTestsResultsInBuildScan by registering(ImportJUnitXmlReports::class) {
    referenceTask.set(visualTests)
    reports.from(fileTree(visualTests.get().testReports))
    dialect.set(JUnitXmlDialect.GENERIC)
  }

  visualTests {
    finalizedBy(includeVisualTestsResultsInBuildScan)
  }

  check {
    dependsOn(vite)
    dependsOn(visualTests)
  }
}
