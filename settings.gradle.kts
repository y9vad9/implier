pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        id("org.jetbrains.kotlin.multiplatform") version "1.7.20-Beta"
        id("com.google.devtools.ksp") version "1.7.20-Beta-1.0.6"
    }
}

rootProject.name = "implier"

includeBuild("build-logic/dependencies")
includeBuild("build-logic/configuration")
includeBuild("build-logic/library-deploy")

include(":ksp")
include(":test")
