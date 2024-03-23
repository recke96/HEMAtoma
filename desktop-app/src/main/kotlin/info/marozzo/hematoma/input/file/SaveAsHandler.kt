/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.file

import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.PickerResult
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.input.EventInputHandlerScope
import info.marozzo.hematoma.utils.writeToFile
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.nio.file.StandardOpenOption

data object SaveAsHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle() {
        val name = getCurrentState().event.name
        postEvent(
            RequestSaveLocation(
                title = "Save As",
                initialDirectory = System.getProperty("user.home")?.let(Path::of),
                extensions = listOf("json")
            ) {
                when (it) {
                    is PickerResult.File -> SaveAt(it.file.resolve("$name.json"), overwrite = true)
                    is PickerResult.Files -> error("Unexpected result for single file request")
                    is PickerResult.Dismissed -> null
                }
            }
        )
    }

    context(EventInputHandlerScope)
    suspend fun handle(input: SaveAt) {
        val (_, event) = getCurrentState()
        sideJob("write-file-${input.path}") {
            Json.writeToFile(
                event,
                input.path,
                if (input.overwrite) StandardOpenOption.TRUNCATE_EXISTING else StandardOpenOption.CREATE_NEW
            ).fold(
                {
                    flogger.atInfo().log("Error saving to file %s: %s", input.path, it)
                    postEvent(ThrowableEvent(it))
                },
                { postInput(SavedAs(input.path)) }
            )
        }
    }

    context(EventInputHandlerScope)
    suspend fun handle(input: SavedAs) {
        updateState { EventState.path.set(it, input.path) }
    }
}
