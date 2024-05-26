plugins {
    `kotlin-dsl`
}

group = "org.kybprototyping.notificationservice"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // To build a Kotlin project with Gradle,
    // you need to add the Kotlin Gradle plugin to your build script file build.gradle(.kts) and configure the project's dependencies there.
    implementation(libs.kotlin.gradle.plugin)
}
