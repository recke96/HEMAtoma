import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.conveyor) apply false
    alias(libs.plugins.detekt)
}

version = System.getenv("RELEASE_VERSION") ?: "0.0.0"

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

        val projectDir = layout.projectDirectory.asFile
        exclude {
            it.file.relativeTo(projectDir).startsWith("build")
        }
        finalizedBy(mergeSarif)
    }
    mergeSarif {
        input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}
