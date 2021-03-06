import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    id(Deps.Plugins.Configuration.Kotlin.Mpp)
    id(Deps.Plugins.Deploy.Id)
}

group = AppInfo.PACKAGE
version = AppInfo.VERSION

allprojects {
    repositories {
        mavenCentral()
    }
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}

val deployProperties = rootProject.file("deploy.properties")

deploy {
    if (!deployProperties.exists())
        ignore = true
    else {
        val properties = loadProperties(deployProperties.absolutePath)
        host = properties["host"] as String?
        user = properties["user"] as String?
        password = properties["password"] as String?
        deployPath = properties["deployPath"] as String?

        componentName = "kotlin"
        group = AppInfo.PACKAGE
        version = AppInfo.VERSION
        artifactId = "implier"
        name = "implier"
        description = "Kotlin codegeneration library for Mutable & Immutable objects from interfaces."
    }
}