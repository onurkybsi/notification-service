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
    "modules:domain:port:notificationtemplaterepository",
    "modules:domain:common",
    "modules:domain:usecase",
    "modules:adapter:primary",
    "modules:common"
)
include("modules:adapter:secondary:notificationtemplaterepository")
findProject(":modules:adapter:secondary:notificationtemplaterepository")?.name = "notificationtemplaterepository"
