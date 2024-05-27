pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "notification-service"

include(
    "modules:domain:model",
    "modules:domain:port:emailstorage",
    "modules:domain:common",
    "modules:infrastructure",
    "modules:domain:usecase"
)
