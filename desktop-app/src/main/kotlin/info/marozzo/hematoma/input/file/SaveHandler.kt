/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.file

import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.contract.ErrorEvent
import info.marozzo.hematoma.input.EventInputHandlerScope
import info.marozzo.hematoma.utils.writeToFile
import kotlinx.serialization.json.Json
import java.nio.file.StandardOpenOption

data object SaveHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle() {
        val (path, event) = getCurrentState()
        if (path == null) {
            flogger.atWarning().log("File was not saved before - no path known")
            return
        }
        sideJob("write-file-$path") {
            Json.writeToFile(event, path, StandardOpenOption.TRUNCATE_EXISTING).onLeft {
                flogger.atInfo().log("Error saving to file %s: %s", path, it)
                postEvent(ErrorEvent(it))
            }
        }
    }
}
