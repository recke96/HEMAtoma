/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers

import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.EventContract
import info.marozzo.hematoma.EventInputHandlerScope
import info.marozzo.hematoma.path
import info.marozzo.hematoma.utils.writeToFile
import kotlinx.serialization.json.Json
import java.nio.file.StandardOpenOption

data object SaveAsHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle(input: EventContract.Input.SaveAs) {
        val (_, event) = getCurrentState()
        sideJob("write-file-${input.path}") {
            Json.writeToFile(event, input.path, StandardOpenOption.CREATE_NEW).onLeft {
                flogger.atInfo().log("Error saving to file %s: %s", input.path, it)
            }
            postInput(EventContract.Input.SavedAs(input.path))
        }
    }

    context(EventInputHandlerScope)
    suspend fun handle(input: EventContract.Input.SavedAs) {
        updateState { EventContract.State.path.set(it, input.path) }
    }
}
