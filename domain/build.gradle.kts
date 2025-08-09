plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    api(libs.bundles.arrow)
    ksp(libs.arrow.optics.ksp)

    implementation(libs.kotlinx.coroutines)
    api(libs.kotlinx.serialization)
    api(libs.kotlinx.immutable.collections)

    detektPlugins(libs.detekt.arrow)
}

detekt {
    buildUponDefaultConfig = true
    basePath = rootProject.layout.projectDirectory.toString()
}
