plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.google.ksp) apply false
    `maven-publish`
}

group = "com.y9vad9.implier"
version = "1.0.5"

kotlin {
    jvm()
    jvmToolchain(21)

    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            groupId = "com.y9vad9.implier"
            artifactId = "implier"
            version = "1.0.5"
        }
    }
}