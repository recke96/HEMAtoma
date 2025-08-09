import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.conveyor) apply false
    alias(libs.plugins.detekt)
}

val mergeSarif by tasks.registering(ReportMergeTask::class) {
    // Avoid the detekt reports directory to prevent overlapping outputs
    val mergedDir = layout.buildDirectory.dir("reports/detekt-merged")
    output.set(mergedDir.map { it.file("detekt.sarif") })
}

allprojects {
    group = "info.marozzo.hematoma"

    apply<DetektPlugin>()

    detekt {
        parallel = true
        buildUponDefaultConfig = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = JvmTarget.JVM_21.target
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

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = true
            jvmTarget = JvmTarget.JVM_21
        }
    }
}
