plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "com.y9vad9.implier"
version = "1.0.5"

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    compileOnly(libs.ksp.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(project(":"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kct.core)
    testImplementation(libs.kct.ksp)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.y9vad9.implier", "ksp", project.version.toString())

    pom {
        name.set("implier ksp-implementation")
        description.set("Kotlin codegeneration library for Mutable & Immutable objects from interfaces.")
        url.set("https://github.com/y9vad9/implier")
        inceptionYear.set("2022")

        licenses {
            license {
                name.set("The MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("y9vad9")
                name.set("Vadym Yaroshchuk")
                url.set("https://github.com/y9vad9/")
            }
        }

        scm {
            url.set("https://github.com/y9vad9/implier")
            connection.set("scm:git:git://github.com/y9vad9/implier.git")
            developerConnection.set("scm:git:ssh://git@github.com/y9vad9/implier.git")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/y9vad9/implier/issues")
        }
    }
}