plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.bundles.arrow)
    ksp(libs.arrow.optics.ksp)

    implementation(libs.kotlinx.coroutines)
    api(libs.kotlinx.serialization)
    api(libs.kotlinx.immutable.collections)
}

detekt {
    buildUponDefaultConfig = true
    basePath = rootProject.layout.projectDirectory.toString()
}
