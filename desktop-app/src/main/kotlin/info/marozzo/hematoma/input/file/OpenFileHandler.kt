/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.file

import arrow.optics.copy
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.PickerResult
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.domain.Event
import info.marozzo.hematoma.input.EventInputHandlerScope
import info.marozzo.hematoma.utils.readFromFile
import kotlinx.serialization.json.Json
import java.nio.file.Path

data object OpenFileHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle() {
        postEvent(
            RequestFileEvent(
                title = "Open Event",
                initialDirectory = System.getProperty("user.home")?.let(Path::of),
                extensions = listOf("json")
            ) {
                when (it) {
                    is PickerResult.File -> OpenFile(it.file)
                    is PickerResult.Files -> error("Unexpected result for single file request")
                    is PickerResult.Dismissed -> null
                }
            }
        )
    }

    context(EventInputHandlerScope)
    suspend fun handle(input: OpenFile) {
        sideJob("read-file-${input.path}") {
            Json.readFromFile<Event>(input.path).fold(
                {
                    flogger.atInfo().log("Error reading file %s: %s", input.path, it)
                    postEvent(ThrowableEvent(it))
                },
                { postInput(OpenedFile(input.path, it)) }
            )
        }
    }

    context(EventInputHandlerScope)
    suspend fun handle(input: OpenedFile) {
        updateState {
            it.copy {
                EventState.path set input.path
                EventState.event set input.event
            }
        }
    }
}
