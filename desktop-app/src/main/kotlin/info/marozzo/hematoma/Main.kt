/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import arrow.continuations.SuspendApp
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.components.CompetitorNameInput
import info.marozzo.hematoma.components.CompetitorsList
import info.marozzo.hematoma.components.Header
import info.marozzo.hematoma.contract.AddCompetitor
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.domain.RegistrationNumber

private val logger = FluentLogger.forEnclosingClass()!!

fun main(args: Array<String>) = SuspendApp {
    logger.atInfo().log("Start HEMAtoma")
    awaitApplication {
        val coroutineScope = rememberCoroutineScope()
        val vm = remember(coroutineScope) { EventViewModel(coroutineScope) }
        val state by vm.observeStates().collectAsState()

        Window(onCloseRequest = this::exitApplication, title = "HEMAtoma") {
            App(state, vm::trySend)
        }
    }
    logger.atInfo().log("Stopping HEMAtoma")
}

@Composable
@Suppress("ModifierMissing") // Is the top-level composable and has no use for modifier
fun App(state: EventState, accept: AcceptFun) = MaterialTheme {
    val nextReg = remember(state.event.competitors) { RegistrationNumber(state.event.competitors.size.inc().toString()).getOrNull() ?: error("Can't happen") }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Header(state, accept, modifier = Modifier.background(MaterialTheme.colors.primarySurface))
        CompetitorNameInput(number = nextReg, onSubmit = { accept(AddCompetitor(nextReg, it)) })
        CompetitorsList(state.event.competitors, modifier = Modifier.fillMaxSize())
    }
}
