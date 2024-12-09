import com.gradle.enterprise.gradleplugin.test.ImportJUnitXmlReports
import com.gradle.enterprise.gradleplugin.test.JUnitXmlDialect
import com.seeq.build.behavior.ConnectorTest
import com.seeq.build.behavior.LighthouseTest
import com.seeq.build.behavior.PlaywrightSystemTest
import com.seeq.build.behavior.SystemTest
import com.seeq.build.sqFolder
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    com.seeq.build.npm.npm
}

val seeqNames by configurations.creating
val javascriptSdk by configurations.creating

val perfTestService = gradle.sharedServices.registrations
    .getByName<BuildServiceRegistration<com.seeq.build.performance.PerformanceTestService, com.seeq.build.performance.PerformanceTestOptions>>(
        "performanceTestService",
    )
    .service

dependencies {
    seeqNames(project(":seeq-utilities", "seeqNames"))
    javascriptSdk(project(":seeq-sdk", "javascriptSdk"))
}

tasks {
    npmInstall {
        dependsOn(":client:packages-webserver:assemble")

        // To upgrade to a new chromedriver:
        // - Use the createChromiumToolChainPackage.js file to create chromedriver toolchain files
        // - Update the following variable:
        val chromedriverRevision = "117.0.5938.88"
        // To upgrade to a new selenium (probably not needed since protractor is done):
        // - Copy the old selenium toolchain folder and update the jar file with the new one that you download
        val operatingSystem = OperatingSystem.current()
        val seleniumSuffix = when {
            operatingSystem.isWindows -> "windows"
            operatingSystem.isLinux -> "linux"
            operatingSystem.isMacOsX -> "osx"
            else -> throw IllegalArgumentException("Unsupported operating system")
        }
        val seleniumToolchainPackage = "$sqFolder/toolchain/selenium-3.141.59-$seleniumSuffix/files"
        if (!file(seleniumToolchainPackage).exists()) {
            throw GradleException(
                "Selenium toolchain package is missing, do you need to run './sq install'?",
            )
        }
        inputs.dir(seleniumToolchainPackage)
        val suffix = when {
            operatingSystem.isWindows -> "64bit-windows"
            operatingSystem.isLinux -> "linux"
            operatingSystem.isMacOsX -> "osx"
            else -> throw IllegalArgumentException("Unsupported operating system")
        }
        val chromedriverToolchainPackage = "$sqFolder/toolchain/chromedriver-$chromedriverRevision-$suffix/files"
        inputs.dir(chromedriverToolchainPackage)
        val fs = serviceOf<FileSystemOperations>()
        doLast {
            fs.copy {
                from(seleniumToolchainPackage, chromedriverToolchainPackage)
                into("node_modules/protractor/node_modules/webdriver-manager/selenium")
            }
        }
    }

    val copyJsSdk by registering(Sync::class) {
        from(javascriptSdk)
        into("sdk")
    }

    val generateJsConstants by registering(com.seeq.build.SharedStringGeneratorTask::class) {
        jsonFile.fileProvider(seeqNames.elements.map { it.single().asFile })
        generate = file("features/support/constants.seeqnames.js")
    }

    val playwrightSystemTest by registering(PlaywrightSystemTest::class) {
        dependsOn(assemble)

        outputs.upToDateWhen {
            false // these tests depend on literally everything, don't bother trying to track
        }

        group = "verification"

        playwrightConfig.set(layout.projectDirectory.file("automated/support/playwright.config.ts"))
        testReports.set(layout.projectDirectory.dir("test-reports/playwright"))
        nodeArgs.add("--max-old-space-size=1536")
    }

    val playwrightSystemTestFailFast by registering(PlaywrightSystemTest::class) {
        dependsOn(assemble)

        outputs.upToDateWhen {
            false // these tests depend on literally everything, don't bother trying to track
        }

        group = "verification"

        playwrightConfig.set(layout.projectDirectory.file("automated/support/playwrightFailFast.config.ts"))
        testReports.set(layout.projectDirectory.dir("test-reports/playwright"))
        nodeArgs.add("--max-old-space-size=1536")
    }

    val dataLabPlaywrightSystemTest by registering(PlaywrightSystemTest::class) {
        dependsOn(assemble)

        outputs.upToDateWhen {
            false // these tests depend on literally everything, don't bother trying to track
        }

        group = "verification"

        playwrightConfig.set(layout.projectDirectory.file("automated/support/dataLabPlaywright.config.ts"))
        testReports.set(layout.projectDirectory.dir("test-reports/playwright"))
        nodeArgs.add("--max-old-space-size=1536")
    }

    val includePlaywrightTestResultsInBuildScan by registering(ImportJUnitXmlReports::class) {
        referenceTask.set(playwrightSystemTest)
        reports.from(fileTree(playwrightSystemTest.get().testReports))
        dialect.set(JUnitXmlDialect.GENERIC)
    }

    playwrightSystemTest {
        finalizedBy(includePlaywrightTestResultsInBuildScan)
    }

    val systemTest by registering(SystemTest::class) {
        dependsOn(assemble)

        outputs.upToDateWhen {
            false // these tests depend on literally everything, don't bother trying to track
        }

        group = "verification"

        protractorConfig.set(layout.projectDirectory.file("protractor.conf.js"))
        testReports.set(layout.projectDirectory.dir("test-reports/$name"))
        nodeArgs.add("--max-old-space-size=5120")
    }

    val includeSystemTestResultsInBuildScan by registering(ImportJUnitXmlReports::class) {
        referenceTask.set(systemTest)
        reports.from(fileTree(systemTest.get().testReports))
        dialect.set(JUnitXmlDialect.GENERIC)
    }

    systemTest {
        finalizedBy(includeSystemTestResultsInBuildScan)
    }

    register<ConnectorTest>("connectorTest") {
        dependsOn(assemble)

        outputs.upToDateWhen {
            false // these tests depend on literally everything, don't bother trying to track
        }

        group = "verification"

        testReports.set(layout.projectDirectory.dir("test-reports/$name"))
        tests.set(
            provider {
                fileTree("connectors/tests") { include("*.js") }.files.joinToString(",") { project.relativePath(it) }
            },
        )
    }

    register<LighthouseTest>("lighthouseTest") {
        dependsOn(assemble)

        outputs.upToDateWhen {
            false // these tests depend on literally everything, don't bother trying to track
        }

        group = "verification"

        usesService(perfTestService)
        performanceTestService.set(perfTestService)
        playwrightConfig.set(layout.projectDirectory.file("automated/support/lighthouse.config.ts"))
        nodeArgs.add("--max-old-space-size=1536")
        testReports.set(layout.projectDirectory.dir("test-reports/$name/workbook"))
    }

    clean {
        dependsOn("cleanCopyJsSdk")
        dependsOn("cleanGenerateJsConstants")
        dependsOn("cleanSystemTest")
        dependsOn("cleanConnectorTest")
    }

    assemble {
        dependsOn(copyJsSdk, generateJsConstants)
    }
}