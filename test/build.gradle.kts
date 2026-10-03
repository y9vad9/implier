plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.google.ksp)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":"))
    testImplementation(projects.ksp)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kct.core)
    testImplementation(libs.kct.ksp)
    kspTest(projects.ksp)
}

tasks.withType<Test> {
    useJUnitPlatform()
}