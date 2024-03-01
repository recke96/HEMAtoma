import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
}

val mergeSarif by tasks.registering(ReportMergeTask::class) {
    output = layout.buildDirectory.file("reports/detekt/merged.sarif")
}

allprojects {
    group = "info.marozzo.hematoma"

    apply<DetektPlugin>()

    detekt {
        parallel = true
        buildUponDefaultConfig = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "21"
        reports {
            sarif.required = true
        }
        basePath = rootDir.absolutePath
        exclude(layout.buildDirectory.toString())
    }
    mergeSarif {
        input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
        mustRunAfter(tasks.withType<Detekt>())
    }
}
