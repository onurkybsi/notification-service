pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "notification-service"

include(
    "modules:domain",
    "modules:adapter",
    "modules:common"
)
