/*
 * Copyright Jakob Ecker, 2025
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.utils

import arrow.fx.coroutines.ResourceScope
import dev.dirs.ProjectDirectories
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tinylog.configuration.Configuration
import org.tinylog.kotlin.Logger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
context(coroutineScope: CoroutineScope, resourceScope: ResourceScope)
suspend fun configureLogging(dirs: ProjectDirectories) {
    val sessionId = Uuid.random().toString()
    val logFile = Path.of(dirs.dataLocalDir, "$sessionId.log").toAbsolutePath()

    Configuration.replace(
        mapOf(
            "writer_console" to "console",
            "writer_console.level" to "debug",

            "writer_json" to "json",
            "writer_json.level" to "info",
            "writer_json.file" to logFile.toString(),
            "writer_json.format" to "LDJSON",
            "writer_json.charset" to "UTF-8",
            "writer_json.buffered" to "true",
            "writer_json.field.time" to "{date:yyyy-MM-dd'T'HH:mm:ss.SSSX}",
            "writer_json.field.session" to sessionId,
            "writer_json.field.severity" to "{level}",
            "writer_json.field.logger" to "{logger}",
            "writer_json.field.class" to "{class}",
            "writer_json.field.tag" to "{tag}",
            "writer_json.field.method" to "{method}",
            "writer_json.field.thread" to "{thread-id}",
            "writer_json.field.body" to "{message-only}",
            "writer_json.field.exception" to "{exception}",
        )
    )

    resourceScope.install(
        acquire = {
            Logger.info("Start HEMAtoma {}", { System.getProperty("app.version") ?: "dev" })
            Logger.info(
                "Running JVM {} {} on {} {} ({})",
                { System.getProperty("java.vm.name") },
                { System.getProperty("java.vm.version") },
                { System.getProperty("os.name") },
                { System.getProperty("os.version") },
                { System.getProperty("os.arch") },
            )
            Logger.debug("Session ID: {}", sessionId)
            Logger.debug("Writing log to {}", logFile)
        },
        release = { _, _ -> Logger.info("Stop HEMAtoma {}", { System.getProperty("app.version") ?: "dev" }) }
    )

    // Cleanup old log files
    coroutineScope.launch(Dispatchers.IO + CoroutineName("Log Cleanup")) {
        runCatching {
            logFile.parent.listDirectoryEntries("*.log")
                .sortedByDescending { Files.getLastModifiedTime(it) }
                .drop(3) // Keep the last 3 log files
                .forEach {
                    Logger.tag("MAINTENANCE").debug("Deleting old log file {}", it)
                    Files.deleteIfExists(it)
                }
        }.onFailure { Logger.tag("MAINTENANCE").warn(it, "Failed to cleanup old log files") }
    }
}
