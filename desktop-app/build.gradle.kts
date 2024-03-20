plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.conveyor)
}

version = System.getenv("RELEASE_VERSION")?.trimStart('v') ?: "0.1.5"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))

    "linuxAmd64"(compose.desktop.linux_x64)
    "windowsAmd64"(compose.desktop.windows_x64)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.filepicker)
    implementation(libs.datatable)

    implementation(libs.bundles.arrow)
    ksp(libs.arrow.optics.ksp)

    implementation(libs.bundles.ballast)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.flogger)
    runtimeOnly(libs.flogger.backend)

    detektPlugins(libs.detekt.compose)
    detektPlugins(libs.detekt.arrow)
}

configurations.all {
    attributes {
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
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
