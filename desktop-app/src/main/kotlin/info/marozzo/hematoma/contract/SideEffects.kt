/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.contract

sealed interface SideEffect

data class ErrorSideEffect(val msg: String) : SideEffect
data class ThrowableSideEffect(val throwable: Throwable) : SideEffect

