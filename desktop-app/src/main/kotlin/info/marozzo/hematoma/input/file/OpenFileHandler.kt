/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.file

import arrow.optics.copy
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.domain.Event
import info.marozzo.hematoma.input.EventInputHandlerScope
import info.marozzo.hematoma.utils.readFromFile
import kotlinx.serialization.json.Json

data object OpenFileHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle(input: OpenFile) {
        sideJob("read-file-${input.path}") {
            Json.readFromFile<Event>(input.path).fold(
                {
                    flogger.atInfo().log("Error reading file %s: %s", input.path, it)
                    postEvent(ErrorEvent(it))
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
