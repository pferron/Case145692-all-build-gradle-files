import com.seeq.build.isCi
import com.seeq.build.isWindows
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.internal.os.OperatingSystem

plugins {
    com.seeq.build.python.venv
    com.seeq.build.base
    com.seeq.build.docker.docker
    com.seeq.build.helm.chart
}

// The gradle plugin for IntelliJ doesn't support specifying the project type or a python SDK,
// so the gradle stuff must be contained in a subdirectory to prevent breaking intellisense
// https://github.com/JetBrains/gradle-idea-ext-plugin/issues/134
val baseDir = projectDir.parent


tasks {

    createVirtualEnv {
        pyproject.set(file("$baseDir/pyproject.toml"))
        lockFile.set(file("$baseDir/uv.lock"))
        venvDir.set(file("$baseDir/.venv"))
    }

    assemble {
        dependsOn(createVirtualEnv)
    }

    dockerBuildContext {
        from("$baseDir/pyproject.toml")
        from("$baseDir/uv.lock")
        from("$baseDir/app") {
            into("app")
            exclude("**/__pycache__/**")
        }
    }

    test {
        environment.set(
            mapOf(
                "PYTHONPATH" to "",
                "SEEQ_URL" to "http://localhost:34216/",
            ),
        )
        mark.set("unit or integration")
        with(sources) {
            from(baseDir)
            include("tests/**")
            exclude("**/__pycache__/**")
        }
        // These tests don't function in CI yet
        enabled = !isCi
    }

}
