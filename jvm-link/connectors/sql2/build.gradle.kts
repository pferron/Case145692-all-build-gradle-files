plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-sql2"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

repositories {
    maven("http://redshift-maven-repository.s3-website-us-east-1.amazonaws.com/release") {
        isAllowInsecureProtocol = true
    }
    flatDir {
        dir("lib")
    }
}

val nonDistributedDrivers by configurations.creating
configurations.providedLibraries {
    extendsFrom(nonDistributedDrivers)
}

dependencies {
    providedLibraries(project(":jvm-link:seeq-link-agent"))
    implementation(project(":jvm-link:seeq-link-connector-commons-tabular"))

    // Whenever we update this version, we must also update the DLL package; see toolchain.json.
    // Additionally, we must check if other connectors are relying on this driver and update them accordingly
    // (e.g. EBX Connector - https://bitbucket.org/seeq12/ad-connectors/src/master/jvm-link-connectors/ExxonEbxSqlConnector_55/)
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.4.1.jre8")
    implementation("org.postgresql:postgresql")
    implementation(":ojdbc8:12.2.0.1")
    // We are able to distribute the Athena Simba JDBC driver as it is licensed under Apache 2.0.
    implementation(":AthenaJDBC42:2.0.31")
    implementation("com.github.jsqlparser:jsqlparser:1.1")
    implementation("com.zaxxer:HikariCP:3.4.5")
    // Snowflake driver is licensed under Apache 2.0.
    implementation("net.snowflake:snowflake-jdbc:3.14.4")
    implementation("com.microsoft.azure:msal4j:1.11.0")

    // SAP Hana has a Developer License: https://tools.hana.ondemand.com/developer-license-3_1.txt
    implementation("com.sap.cloud.db.jdbc:ngdbc:2.7.9")

    // Databricks now allows redistribution (https://www.databricks.com/legal/jdbc-odbc-driver-license)
    implementation("com.databricks:databricks-jdbc:2.6.33")

    // For Snowflake OAuth 2.0 authentication
    implementation("com.nimbusds:oauth2-oidc-sdk:10.1")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.2")
    /** nimbusds libraries use this bouncycastle dependency as an optional dependency,
     * so its version must be compatible with nimbusds's specified versions
     * https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support
     */
    implementation("org.bouncycastle:bcpkix-jdk18on")

    testImplementation(project(":seeq:common-azure-keyvault"))

    /** The MySQL JDBC driver is GPL-licensed, and cannot be distributed with Seeq Server. */
    nonDistributedDrivers("com.mysql:mysql-connector-j:8.2.0")
    /** The Redshift JBDC driver cannot be distributed without permission from Amazon. */
    nonDistributedDrivers("com.amazon.redshift:redshift-jdbc42:1.2.15.1025")
    /** The Vertica JBDC driver cannot be distributed without permission from Vertica. */
    nonDistributedDrivers(":vertica-jdbc:9.1.1-0")
    /**  We cannot distribute the timestream driver without permission from Amazon. */
    nonDistributedDrivers(":timestream-jdbc:1.1.3")
    /**  The Denodo JDBC driver cannot be distributed without permission from Denodo. */
    nonDistributedDrivers(":denodo-v8-20220126")
}

coverage {
    threshold.set(0.70)
}

tasks {
    val copyNonDistributedDrivers by creating(Sync::class) {
        from(nonDistributedDrivers)
        destinationDir = file("$buildDir/nonDistributedLib")
    }

    jar {
        dependsOn(copyNonDistributedDrivers)
    }
}

tasks {
    withType<Test>().configureEach {
        // PrecisionTestRedshiftIT failing on reflective access otherwise.
        // redshift driver needs this
        jvmArgs("--add-opens", "java.base/java.net=ALL-UNNAMED")
        // PrecisionTestSnowflakeIT failing on reflective access otherwise.
        // snowflake driver needs this
        jvmArgs("--add-opens", "java.base/java.nio=ALL-UNNAMED")
    }
}