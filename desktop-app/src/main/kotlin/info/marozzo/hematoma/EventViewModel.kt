/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import com.copperleaf.ballast.*
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import info.marozzo.hematoma.utils.FluentLoggingInterceptor
import kotlinx.coroutines.CoroutineScope

typealias EventEventHandlerScope = EventHandlerScope<EventContract.Input, Nothing, EventContract.State>

class EventViewModel(scope: CoroutineScope) : BasicViewModel<
        EventContract.Input,
        Nothing,
        EventContract.State
        >(
    config = BallastViewModelConfiguration.Builder().apply {
        interceptors += FluentLoggingInterceptor<EventContract.Input, Nothing, EventContract.State>()
        inputStrategy = FifoInputStrategy()
    }.withViewModel(EventContract.State(), EventInputHandler(), "HEMAtoma").build(),
    eventHandler = EventEventHandler(),
    coroutineScope = scope
)

class EventEventHandler : EventHandler<EventContract.Input, Nothing, EventContract.State> {
    override suspend fun EventEventHandlerScope.handleEvent(event: Nothing) =
        Unit
}
