import org.jooq.tools.json.JSONObject

plugins {
    com.seeq.build.base
    com.seeq.build.helm.chart
}

tasks {
    helmDependenciesUpdate {
        val relativeDependencies: Provider<List<File>> = project.provider {
            val chartDependencies = (
                org.yaml.snakeyaml.Yaml().load(
                    chartYaml.get().asFile
                        .readText(),
                ) as Map<*, *>
                )["dependencies"] as List<*>
            val dependencyFiles: List<String> = chartDependencies
                .map {
                    JSONObject(it as Map<*, *>).toMap()["repository"].toString()
                        .replace("file://../../..", "${rootProject.projectDir}")
                }
            dependencyFiles.filter { it.contains("${rootProject.projectDir}") }.map { File(it) }
        }
        inputs.files(relativeDependencies).withPathSensitivity(PathSensitivity.RELATIVE).ignoreEmptyDirectories()
        dependsOn(":compute:compute-engine-service:helmDependenciesUpdate")
        dependsOn(":cache:cache-persistent-service-quantity:helmDependenciesUpdate")
        dependsOn(":compute:compute-formula-support-service:helmDependenciesUpdate")
        dependsOn(":cache:cache-persistent-service-series:helmDependenciesUpdate")
        dependsOn(":seeq-in-a-box:helmDependenciesUpdate")
    }
}