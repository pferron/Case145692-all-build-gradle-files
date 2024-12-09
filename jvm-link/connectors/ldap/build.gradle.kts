plugins {
    com.seeq.build.link.jvm.connector
}

description = "seeq-link-connector-ldap"
version = "100.0.${project.properties["seeqDaysVersion"]}.${project.properties["seeqSecondsVersion"]}" +
    "${project.properties["seeqVersionSuffix"]}"
connector.minSeeqLinkSdkVersion = "100.0.0.0" // Minimum Seeq Link SDK version required by this connector

dependencies {
    // LDAP library for connecting to LDAP & Active Directory
    // We cannot use 2.1.0 due to a TLSv1.3 implementation bug (either in the client or in the ldapsdk server we use
    // in tests) - CRAB-28807.
    // See also https://issues.apache.org/jira/browse/DIRAPI-381 and https://github.com/pingidentity/ldapsdk/issues/122.
    // Note: We are not sure if the problem is in ldapsdk or apache.directory.api and it is safer to keep using 2.0.x
    // meaning TLSv1.2 to avoid such a problem in production.
    implementation("org.apache.directory.api:api-all:2.0.2")

    implementation("org.springframework.security.kerberos:spring-security-kerberos-core:1.0.1.RELEASE")
    implementation("org.springframework.security:spring-security-core:5.7.12")

    // LDAP library with in-memory server, used only for testing
    testImplementation("com.unboundid:unboundid-ldapsdk:6.0.5")
}

coverage {
    threshold.set(0.74)
}