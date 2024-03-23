/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.contract

import info.marozzo.hematoma.PickerResult
import java.nio.file.Path

sealed interface Event

data class ErrorEvent(val msg: String) : Event
data class ThrowableEvent(val throwable: Throwable) : Event

data class RequestFileEvent(
    val title: String? = null,
    val initialDirectory: Path? = null,
    val extensions: List<String> = emptyList(),
    val toInput: (PickerResult) -> Input?
) : Event
