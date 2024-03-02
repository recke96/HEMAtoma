/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.material.SnackbarHostState
import com.copperleaf.ballast.*
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import info.marozzo.hematoma.contract.Error
import info.marozzo.hematoma.contract.Event
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Input
import info.marozzo.hematoma.input.EventInputHandler
import info.marozzo.hematoma.utils.FluentLoggingInterceptor
import info.marozzo.hematoma.utils.discard
import kotlinx.coroutines.CoroutineScope

typealias EventEventHandlerScope = EventHandlerScope<Input, Event, EventState>

class EventViewModel(scope: CoroutineScope, snackbar: SnackbarHostState) : BasicViewModel<Input, Event, EventState>(
    config = BallastViewModelConfiguration.Builder().apply {
        interceptors += FluentLoggingInterceptor<Input, Event, EventState>()
        inputStrategy = FifoInputStrategy()
    }.withViewModel(EventState(), EventInputHandler(), "HEMAtoma").build(),
    eventHandler = EventEventHandler(snackbar),
    coroutineScope = scope
)

class EventEventHandler(private val snackbar: SnackbarHostState) : EventHandler<Input, Event, EventState> {
    override suspend fun EventEventHandlerScope.handleEvent(event: Event): Unit = when(event) {
        is Error -> snackbar.showSnackbar(event.msg).discard()
    }
}
