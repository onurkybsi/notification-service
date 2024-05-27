pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "notification-service"

include(
    "modules:domain:model",
    "modules:domain:port:emailstorage",
    "modules:domain:common",
    "modules:domain:usecase",
    "modules:infrastructure:primary"
)