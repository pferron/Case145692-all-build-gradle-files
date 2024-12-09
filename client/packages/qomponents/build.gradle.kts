import com.seeq.build.npm.Jest
import com.seeq.build.npm.Tsc
import com.seeq.build.npm.npmExecutable
import com.seeq.build.qomponents.Rollup

plugins {
    com.seeq.build.npm.`node-module`
}

val qomponents by configurations.creating

tasks {

    val rollupSources = fileTree("src")

    val rollup by registering(Rollup::class) {
        inputs.file("postcss.config.cjs").withPathSensitivity(PathSensitivity.NAME_ONLY)
        inputs.file("rollup.config.js").withPathSensitivity(PathSensitivity.NAME_ONLY)
        inputs.file("tailwind.config.cjs").withPathSensitivity(PathSensitivity.NAME_ONLY)
        inputs.file("tsconfig.json").withPathSensitivity(PathSensitivity.NAME_ONLY)
        sources.from(rollupSources)
        distributions.from("dist")
    }

    val tsc by registering(Tsc::class) {
        description = "Checks the project for typescript errors"
        sources.from("src")
        inputs.file("tsconfig.json").withPathSensitivity(PathSensitivity.NAME_ONLY)
    }

    withType<Jest> {
        roots.add("src")
        inputs.file("tsconfig.json").withPathSensitivity(PathSensitivity.NAME_ONLY)
        inputs.files(rollupSources).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
    }

    artifacts {
        add(qomponents.name, rollup)
    }

    assemble {
        dependsOn(tsc)
        dependsOn(rollup)
    }

    val copyToWebserver by registering(Copy::class) {
        dependsOn(rollup)

        from("dist")
        into("../webserver/node_modules/@seeqdev/qomponents/dist")
    }

    register("npmPublish", Exec::class) {
        description = "Publishes to npm and increments package.json patch version"
        doFirst {
            if (!projectDir.resolve(".npmrc").exists()) {
                throw GradleException(
                    "Could not find .npmrc file. Make sure you copy .npmrc_example, rename it to " +
                        ".npmrc and populate it with the correct token from Keeper.",
                )
            }
        }

        dependsOn(rollup)

        workingDir(project.projectDir)
        executable(npmExecutable)
        args("publish")
        args("--access", "public")

        doLast {
            exec {
                workingDir(project.projectDir)
                executable(npmExecutable)
                args("version")
                args("patch")
            }
        }
    }
}