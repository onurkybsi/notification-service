plugins {
    kotlin("jvm") version "1.9.24"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
}