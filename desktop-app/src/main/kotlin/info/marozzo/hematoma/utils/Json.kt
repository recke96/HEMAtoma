/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.utils

import arrow.core.raise.Effect
import arrow.core.raise.effect
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Json.writeToFile(
    value: T,
    path: Path,
    vararg options: OpenOption
): Effect<Nothing, Unit> = effect {
    withContext(Dispatchers.IO + CoroutineName("JSON Writer")) {
        encodeToStream(value, path.outputStream(*options))
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Json.readFromFile(path: Path): Effect<Nothing, Unit> = effect {
    decodeFromStream(path.inputStream(StandardOpenOption.READ))
}
