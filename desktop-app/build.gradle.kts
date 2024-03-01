plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.filepicker)
    
    implementation(libs.bundles.arrow)
    ksp(libs.arrow.optics.ksp)

    implementation(libs.bundles.ballast)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
    
    detektPlugins(libs.detekt.compose)
}

compose.desktop {
    application {
        mainClass = "info.marozzo.hematoma.MainKt"
    }
}

detekt {
    buildUponDefaultConfig = true
    basePath = rootProject.layout.projectDirectory.toString()
}
