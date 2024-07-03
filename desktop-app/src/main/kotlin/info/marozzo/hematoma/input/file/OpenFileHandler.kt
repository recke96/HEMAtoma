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
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.serialization.json.Json

data object OpenFileHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle() {
        sideJob("open") {
            val file = FileKit.pickFile(
                title = "Open Event",
                type = PickerType.File(extensions = listOf("json")),
            )?.file?.toPath() ?: return@sideJob

            Json.readFromFile<Event>(file).fold(
                {
                    flogger.atWarning().withCause(it).log("Error reading file %s", file)
                    postEvent(ThrowableEvent(it))
                },
                { postInput(OpenedFile(file, it)) }
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
