/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.file

import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.contract.ErrorEvent
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.SavedAt
import info.marozzo.hematoma.contract.path
import info.marozzo.hematoma.input.EventInputHandlerScope
import info.marozzo.hematoma.utils.writeToFile
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.StandardOpenOption

data object SaveHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    suspend fun EventInputHandlerScope.handleSave() {
        val (path, event, _) = getCurrentState()
        if (path != null) {
            sideJob("save") {
                Json.writeToFile(event, path, StandardOpenOption.TRUNCATE_EXISTING).onLeft {
                    flogger.atWarning().withCause(it).log("Failed to write %s", path)
                    postEvent(ErrorEvent("Failed to save file."))
                }
            }
        }
    }

    suspend fun EventInputHandlerScope.handle() {
        val (_, event) = getCurrentState()
        sideJob("save-as") {
            val file = FileKit.saveFile(
                baseName = event.name.toString(),
                extension = "json",
                bytes = Json.encodeToString(event).toByteArray(),
            )
            if (file == null) {
                postEvent(ErrorEvent("Failed to save file."))
            } else {
                postInput(SavedAt(file.file.toPath()))
            }
        }
    }

    suspend fun EventInputHandlerScope.handle(input: SavedAt) {
        updateState { EventState.path.set(it, input.path) }
    }
}
