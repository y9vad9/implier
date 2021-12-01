plugins {
    id(Deps.Plugins.KSP.Id)
    id(Deps.Plugins.Configuration.Kotlin.Jvm)
}

sourceSets {
    test {
        java {
            srcDir("build/generated/ksp/test/kotlin")
        }
    }
}

dependencies {
    implementation(project(":"))
    implementation(Deps.Libs.Kotlin.JUnit)
    kspTest(project(":ksp"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}