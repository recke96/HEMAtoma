import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
    id("dev.hydraulic.conveyor") version "1.5"
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

        nativeDistributions {

            modules("jdk.unsupported", "sun.misc")

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
