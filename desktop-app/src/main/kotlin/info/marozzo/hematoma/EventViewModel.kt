/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import com.copperleaf.ballast.*
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import info.marozzo.hematoma.contract.Input
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.inputhandlers.EventInputHandler
import info.marozzo.hematoma.utils.FluentLoggingInterceptor
import kotlinx.coroutines.CoroutineScope

typealias EventEventHandlerScope = EventHandlerScope<Input, Nothing, EventState>

class EventViewModel(scope: CoroutineScope) : BasicViewModel<Input, Nothing, EventState>(
    config = BallastViewModelConfiguration.Builder().apply {
        interceptors += FluentLoggingInterceptor<Input, Nothing, EventState>()
        inputStrategy = FifoInputStrategy()
    }.withViewModel(EventState(), EventInputHandler(), "HEMAtoma").build(),
    eventHandler = EventEventHandler(),
    coroutineScope = scope
)

class EventEventHandler : EventHandler<Input, Nothing, EventState> {
    override suspend fun EventEventHandlerScope.handleEvent(event: Nothing) =
        Unit
}
