/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.contract

sealed interface Event

data class ErrorEvent(val msg: String) : Event
data class ThrowableEvent(val throwable: Throwable) : Event

