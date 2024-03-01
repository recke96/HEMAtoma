/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import arrow.continuations.SuspendApp
import info.marozzo.hematoma.components.Header


fun main(args: Array<String>) = SuspendApp {
    awaitApplication {
        val coroutineScope = rememberCoroutineScope()
        val vm = remember(coroutineScope) { EventViewModel(coroutineScope) }
        val state by vm.observeStates().collectAsState()

        Window(onCloseRequest = this::exitApplication, title = "HEMAtoma") {
            App(state, vm::trySend)
        }
    }
}

@Composable
@Suppress("ModifierMissing") // Is the top-level composable and has no use for modifier
fun App(state: EventContract.State, accept: AcceptFun) = MaterialTheme {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Header(state, accept, modifier = Modifier.background(MaterialTheme.colors.primarySurface))
        Box(Modifier.widthIn(720.dp, 1080.dp).fillMaxHeight()) {
            BasicText("Hello!")
        }
    }
}
