/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.material3.SnackbarHostState
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.withViewModel
import info.marozzo.hematoma.contract.Event
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Input
import info.marozzo.hematoma.event.EventEventHandler
import info.marozzo.hematoma.input.EventInputHandler
import info.marozzo.hematoma.utils.FluentLoggingInterceptor
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

