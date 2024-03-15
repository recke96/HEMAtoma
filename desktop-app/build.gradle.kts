import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
}

version = "0.2.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))

    implementation(compose.desktop.currentOs) {
        exclude(compose.material)
    }
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

compose.desktop {
    application {
        mainClass = "info.marozzo.hematoma.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Exe)
            packageName = rootProject.name
            description = "Tournament planner for HEMA tournaments of the club 'Fior della Spada'"
            copyright = "© 2024 Jakob Ecker. All rights reserved."
            licenseFile = rootProject.file("LICENCE")

            windows {
                console = false
                upgradeUuid = "31E152a4-C1A7-4465-9891-5CC18E54851B"
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    basePath = rootProject.layout.projectDirectory.toString()
}
