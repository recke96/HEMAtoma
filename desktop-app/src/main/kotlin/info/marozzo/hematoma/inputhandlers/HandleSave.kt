/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers

import info.marozzo.hematoma.EventInputHandlerScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.nio.file.StandardOpenOption
import kotlin.io.path.outputStream

@OptIn(ExperimentalSerializationApi::class)
suspend fun EventInputHandlerScope.handleSave(): Unit {
    val (path, event) = getCurrentState()
    if (path == null) {
        logger.info("File was not saved before - no path known")
        return
    }

    withContext(Dispatchers.IO + CoroutineName("JSON Writer")) {
        Json.encodeToStream(event, path.outputStream(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
    }
}
