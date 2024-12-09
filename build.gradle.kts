import org.jetbrains.gradle.ext.ActionDelegationConfig
import org.jetbrains.gradle.ext.ActionDelegationConfig.TestRunner
import org.jetbrains.gradle.ext.ProjectSettings

// Workaround for https://youtrack.jetbrains.com/issue/KT-36331.
// Kotlin is already on the classpath (through buildSrc),
// but for some reason IDEA doesn't understand that.
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        val kotlinVersion = org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    com.seeq.build.base
    com.seeq.build.performance.`regression-detection`
    idea
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("io.github.gradle-nexus.publish-plugin")
}

apply { from("gradle/fix-idea-248929.gradle") }

// Needed by nexusPublishing extension
group = "com.seeq"

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

idea {
    project {
        this as ExtensionAware
        jdkName = "Java ${JavaVersion.current().majorVersion}"
        configure<ProjectSettings> {
            this as ExtensionAware
            configure<ActionDelegationConfig> {
                delegateBuildRunToGradle = true
                testRunner = TestRunner.GRADLE
            }
        }
    }
    module {
        excludeDirs.add(file("appserver/test-data-dir"))
        excludeDirs.add(file("behavior/node_modules"))
        excludeDirs.add(file("behavior/sdk/docs"))
        excludeDirs.add(file("connector-sdk/image"))
        excludeDirs.add(file("connector-sdk/java/libraries"))
        excludeDirs.add(file("data-lab/orchestrator/coverage"))
        excludeDirs.add(file("data-lab/orchestrator/node_modules"))
        excludeDirs.add(file("data-lab/orchestrator/test-reports"))
        excludeDirs.add(file("ide-settings"))
        excludeDirs.add(file("licensing/image"))
        excludeDirs.add(file("log"))
        excludeDirs.add(file("net-link/.vs"))
        excludeDirs.add(file("net-link/image"))
        excludeDirs.add(file("product/image"))
        excludeDirs.add(file("product/installer"))
        excludeDirs.add(file("product/node_modules"))
        excludeDirs.add(file("renderer/coverage"))
        excludeDirs.add(file("renderer/coverage-jest"))
        excludeDirs.add(file("renderer/image"))
        excludeDirs.add(file("renderer/log"))
        excludeDirs.add(file("renderer/node_modules"))
        excludeDirs.add(file("renderer/test-reports"))
        excludeDirs.add(file("sdk/image"))
        excludeDirs.add(file("sdk/pypi/build"))
        excludeDirs.add(file("sdk/pypi/seeq-spy"))
        excludeDirs.add(file("sdk/target/python"))
        excludeDirs.add(file("sdk/target/typescript-react"))
        excludeDirs.add(file("sk8board/app/client/node_modules"))
        excludeDirs.add(file("sk8board/app/client/dist"))
        excludeDirs.add(file("site-packages"))
        excludeDirs.add(file("site-packages/x64/dev"))
        excludeDirs.add(file("site-packages/x64/prod"))
        excludeDirs.add(file("sq-run-data-dir"))
        excludeDirs.add(file("performance/image"))
        excludeDirs.add(file("supervisor/image"))
        excludeDirs.add(file("supervisor/log"))
        excludeDirs.add(file("target"))
        excludeDirs.add(file("test-with"))
        excludeDirs.add(file("client/packages/webserver/.cache-loader"))
        excludeDirs.add(file("client/packages/webserver/app/node_modules"))
        excludeDirs.add(file("client/packages/webserver/coverage"))
        excludeDirs.add(file("client/packages/webserver/coverage-jasmine"))
        excludeDirs.add(file("client/packages/webserver/coverage-jest"))
        excludeDirs.add(file("client/packages/webserver/coverage-karma"))
        excludeDirs.add(file("client/packages/webserver/dist"))
        excludeDirs.add(file("client/packages/webserver/image"))
        excludeDirs.add(file("client/packages/webserver/log"))
        excludeDirs.add(file("client/packages/webserver/node_modules"))
        excludeDirs.add(file("client/packages/webserver/test-reports"))
    }
}

// The overall composeUp task lives in `:dev-compose`, but it is convenient to invoke it from the root project
gradle.taskGraph.whenReady {
    for (exclusiveTask in listOf(":dev-compose:composeUp", ":dev-compose:composeDown", ":dev-compose:composeClean")) {
        if (hasTask(exclusiveTask)) {
            val taskName = ":" + exclusiveTask.substringAfterLast(':')
            allTasks.forEach {
                if (it.path.endsWith(taskName) && it.path != exclusiveTask) {
                    logger.trace("Skipping ${it.path} because $exclusiveTask was specified")
                    it.enabled = false
                }
            }
        }
    }
}