plugins {
    com.seeq.build.`kotlin-module`
    com.seeq.build.link.jvm.`link-module`
    com.seeq.build.lombok
}

description = "commons-tabular"

dependencies {
    implementation(project(":jvm-link:seeq-link-sdk"))
}

coverage {
    threshold.set(0.42)
}