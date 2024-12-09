import com.seeq.build.crossPlatformCommandLine
import com.seeq.build.performance.Locust
import com.seeq.build.performance.PerformanceTestOptions
import com.seeq.build.performance.PerformanceTestService
import com.seeq.build.performance.PytestBenchmark

// This build file is in a nested folder, because IDEA does not support Python and Gradle modules at the same time.
val baseDir = file("..")
val pythonPath = baseDir.absolutePath + File.pathSeparator + System.getenv("PYTHONPATH")
val perfTestService = gradle.sharedServices.registrations
    .getByName<BuildServiceRegistration<PerformanceTestService, PerformanceTestOptions>>(
        "performanceTestService",
    )
    .service
tasks {
    withType<PytestBenchmark>().configureEach {
        outputs.upToDateWhen { false }
        performanceTestService.set(perfTestService)
        environment.put("PYTHONPATH", pythonPath)
        results.set(file("$baseDir/target/test-results/$name"))
    }
    register<PytestBenchmark>("macroBenchmarks") {
        tests.set(File(baseDir, "generalperf/scenarios"))
    }
    register<PytestBenchmark>("volumeBenchmarks") {
        tests.set(File(baseDir, "volume"))
    }
    register<PytestBenchmark>("metadataSyncBenchmark") {
        description = "Runs the large metadata sync test. Can be run as is or with the following parameters:\n\n\t" +
            "`gradlew metadataSyncBenchmark --pytest-args=\"--itemCount <> --archiveCount <> --testTimeout <>\"`" +
            "\n\nwhere:" +
            "\n\t* `itemCount`: Number of items to write from the Stress connector (Default = 25,000,000)" +
            "\n\t* `archiveCount`: Number of items to archive from the Stress connector (Default = 1,000,000)" +
            "\n\t* `testTimeout`: Maximum time (in hours) for the test to complete (Default = 192)"
        tests.set(File(baseDir, "metadata"))
    }
    register<PytestBenchmark>("integratedSecurityStressBenchmark") {
        description =
            "Runs the Integrated Security Stress test, a variant of the Large Metadata Sync test that tests " +
            "access control sync and changes on very large asset trees.\n " +
            "Can be run as is or with the following parameters:\n\n\t" +
            "`gradlew integratedSecurityStressBenchmark --pytest-args=\"--itemCount <> --testTimeout <>\"`" +
            "\n\nwhere:" +
            "\n\t* `itemCount`: Number of items to write from the Stress connector (Default = 25,000,000)" +
            "\n\t* `testTimeout`: Maximum time (in hours) for the test to complete (Default = 192)"
        tests.set(File(baseDir, "integrated-security-stress"))
    }
    register<PytestBenchmark>("concurrentMetadataSyncBenchmark") {
        description =
            "Runs the Concurrent Metadata Sync test to measure performance when multiple connectors are " +
            "posting data simultaneously.\n"
        "Can be run as is or with the following parameters:\n\n\t" +
            "`gradlew concurrentMetadataSyncBenchmark --pytest-args=\"--connectionCount <> --itemCount <> " +
            "--userGroupsCount <> --testTimeout <>\"`" +
            "\n\nwhere:" +
            "\n\t* `connectionCount`: Number of concurrent connections from Stress connector (Default = 100)" +
            "\n\t* `itemCount`: Number of items to write per Stress connector (Default = 100,000)" +
            "\n\t* `userGroupsCount`: Number of user groups to write per Stress connector (Default = 20)" +
            "\n\t* `testTimeout`: Maximum time (in hours) for the test to complete (Default = 4)"
        tests.set(File(baseDir, "concurrent-metadata"))
    }
    register<PytestBenchmark>("flatDatasourceAclBenchmark") {
        description =
            "Runs the Flat Datasource ACL test to measure performance when setting an ACL on a 'flat' datasource " +
            "with no asset structure\n"
        "Can be run as is or with the following parameters:\n\n\t" +
            "`gradlew flatDatasourceAclBenchmark --pytest-args=\"--signalCount <> --testTimeout <>\"`" +
            "\n\nwhere:" +
            "\n\t* `signal`: Number of signals to index to the datasource (Default = 2,000,000)" +
            "\n\t* `testTimeout`: Maximum time (in hours) for the test to complete (Default = 4)"
        tests.set(File(baseDir, "flat-datasource-acl"))
    }
    register<PytestBenchmark>("treemapBenchmark") {
        description =
            "Runs the Treemap Benchmark test to measure the performance of loading multiple Treemaps concurrently\n"
        "Can be run as is or with the following parameters:\n\n\t" +
            "`gradlew treemapBenchmark --pytest-args=\"--itemCount <> --numParallelRequests <> --testTimeout " +
            "<>\"`" +
            "\n\nwhere:" +
            "\n\t* `itemCount`: Number of items to write per Stress connector (Default = 200,000)" +
            "\n\t* `numParallelRequests`: Number of parallel requests to the treemap (Default = 5)" +
            "\n\t* `testTimeout`: Maximum time (in hours) for the test to complete (Default = 4)"
        tests.set(File(baseDir, "treemap"))
    }
    register<PytestBenchmark>("formulaAcrossAssetsBenchmark") {
        description =
            "Runs the Formula Across Assets Benchmark test to measure the performance of loading a formula across" +
            " many assets. " +
            "Can be run as is or with the following parameters:\n\n\t" +
            "`gradlew formulaAcrossAssetsBenchmark --pytest-args=\"--itemCount <> --testTimeout <>\"`" +
            "\n\nwhere:" +
            "\n\t* `itemCount`: Number of items to write per Stress connector (Default = 200,000)" +
            "\n\t* `testTimeout`: Maximum time (in hours) for the test to complete (Default = 4)"
        tests.set(File(baseDir, "formula-across-assets"))
    }
    register<PytestBenchmark>("workloadMonitoringExtraction") {
        dependsOn(":seeq-sdk:buildPython")

        description = "Gets the runtimes from workloads from the workload monitoring server.\n " +
            "The WORKLOAD_MONITORING_ACCESS_KEY and WORKLOAD_MONITORING_PASSWORD " +
            "environment variables must be set with a valid access key/password combination for the server. Must be " +
            "run with the following parameters:\n\n\t" +
            "gradlew workloadMonitoringExtraction --pytest-args=\"--server_url <> --monitoring_workbook_id <>\"" +
            "\n\nwhere:" +
            "\n\t* `server_url`: The server to from which to pull the workload numbers" +
            "\n\t* `monitoring_workbook_id`: The ID of the workbook where the workload worksheets exist"
        tests.set(File(baseDir, "workload-monitoring"))
    }
    withType<Locust>().configureEach {
        outputs.upToDateWhen { false }
        performanceTestService.set(perfTestService)
        environment.put("PYTHONPATH", pythonPath)
        tests.set(File(baseDir, "load"))
        reportDir.set(File(baseDir, "target/test-results/locust"))
        results.set(file("$baseDir/target/test-results/$name"))
    }
    register<Locust>("basicLoadTest") {
        description =
            "Runs a basic load test and captures the time it takes to pull a complex customer workbook." +
            "\nThe results of the workbook pull are reported to the performance database and evaluated using" +
            "\nthe automated regression detection algorithms."
        configFile.set(File(baseDir, "load/locustfiles/locust_basic.conf"))
    }

    register<Locust>("sdlFunctionsLoadTests") {
        configFile.set(File(baseDir, "load/locustfiles/locust_dataLab_functions.conf"))
    }

    register<Locust>("mlServiceLoadTests") {
        configFile.set(File(baseDir, "load/locustfiles/locust_ml_service.conf"))
    }

    val generatePythonGrpcBindings by registering(Exec::class) {
        crossPlatformCommandLine(
            "python -m grpc_tools.protoc " +
                "--proto_path=$rootDir/compute/engine/contract/src/main/proto " +
                "$rootDir/compute/engine/contract/src/main/proto/*.proto " +
                "--python_out=$rootDir/performance/load/common/stubs/compute " +
                "--grpc_python_out=$rootDir/performance/load/common/stubs/compute",
        )
    }

    register<Locust>("computeServiceLoadTests") {
        configFile.set(File(baseDir, "load/locustfiles/locust_compute.conf"))
        dependsOn(generatePythonGrpcBindings)
    }
}