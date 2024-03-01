/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers

import arrow.optics.copy
import info.marozzo.hematoma.*
import info.marozzo.hematoma.domain.Event
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.StandardOpenOption
import kotlin.io.path.inputStream

@OptIn(ExperimentalSerializationApi::class)
suspend fun EventInputHandlerScope.handleOpenFile(input: EventContract.Input.OpenFile): Unit {
    val event = withContext(Dispatchers.IO + CoroutineName("JSON Reader")) {
        Json.decodeFromStream<Event>(input.path.inputStream(StandardOpenOption.READ))
    }

    updateState {
        it.copy {
            EventContract.State.path set input.path
            EventContract.State.event set event
        }
    }
}
