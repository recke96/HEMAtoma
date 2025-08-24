plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.conveyor)
}

version = providers.gradleProperty("releaseVersion").getOrElse("0.0.0")

dependencies {
    implementation(project(":domain"))

    linuxAmd64(compose.desktop.linux_x64)
    windowsAmd64(compose.desktop.windows_x64)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)
    implementation(libs.filekit)
    implementation(libs.datatable)

    implementation(libs.bundles.arrow)
    ksp(libs.arrow.optics.ksp)

    implementation(libs.bundles.ballast)
    implementation(libs.bundles.orbit)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.flogger)
    runtimeOnly(libs.flogger.backend)

    detektPlugins(libs.detekt.compose)
    detektPlugins(libs.detekt.arrow)
}

val uiAttr = Attribute.of("ui", String::class.java)
configurations.named { it in setOf("linuxAmd64", "windowsAmd64") }.configureEach {
    attributes {
        attribute(uiAttr, "awt")
    }
}

compose.desktop {
    application {
        mainClass = "info.marozzo.hematoma.MainKt"
    }
}

compose.resources {
    packageOfResClass = "info.marozzo.hematoma.resources"
}

detekt {
    buildUponDefaultConfig = true
    basePath = rootProject.layout.projectDirectory.toString()
}
