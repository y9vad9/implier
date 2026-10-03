plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = "com.y9vad9.implier"
version = "1.0.5"

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.ksp.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(project(":"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.y9vad9.implier"
            artifactId = "ksp"
            version = "1.0.5"
        }
    }
}