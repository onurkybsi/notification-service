import org.jetbrains.kotlin.gradle.dsl.JvmTarget

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

plugins {
    kotlin("jvm") version "1.9.25"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
