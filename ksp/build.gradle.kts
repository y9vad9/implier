plugins {
    id(Deps.Plugins.Configuration.Kotlin.Mpp)
    id(Deps.Plugins.Deploy.Id)
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(Deps.Libs.KSP.Api)
                implementation(Deps.Libs.KotlinPoet.KotlinPoet)
                implementation(Deps.Libs.KotlinPoet.KSP)
                implementation(Deps.Libs.Kotlin.Reflection)
                implementation(project(":"))
            }
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
        val properties = org.jetbrains.kotlin.konan.properties.loadProperties(deployProperties.absolutePath)
        host = properties["host"] as String?
        user = properties["user"] as String?
        password = properties["password"] as String?
        deployPath = properties["deployPath"] as String?

        componentName = "kotlin"
        group = AppInfo.PACKAGE
        version = AppInfo.VERSION
        artifactId = "ksp"
        name = "implier ksp-implementation"
        description = "Kotlin codegeneration library for Mutable & Immutable objects from interfaces."
    }
}