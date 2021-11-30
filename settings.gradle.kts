pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "kotlin-project-template"

includeBuild("buildUtils/dependencies")
includeBuild("buildUtils/configuration")
//includeBuild("buildUtils/library-deploy")
