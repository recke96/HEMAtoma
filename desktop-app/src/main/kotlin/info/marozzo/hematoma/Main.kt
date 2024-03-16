/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.zIndex
import arrow.continuations.SuspendApp
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.components.*
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Save
import info.marozzo.hematoma.contract.Screen
import java.awt.Dimension

private const val FOREGROUND = 10f
private val logger = FluentLogger.forEnclosingClass()!!

fun main(args: Array<String>) = SuspendApp {
    logger.atInfo().log("Start HEMAtoma %s", object {}::class.java.`package`.implementationVersion)
    awaitApplication {
        val coroutineScope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }
        val vm = remember(coroutineScope) { EventViewModel(coroutineScope, snackbar) }
        val state by vm.observeStates().collectAsState()

        CompositionLocalProvider(LocalAccept provides vm::trySend) {

            Window(onCloseRequest = this::exitApplication, title = "HEMAtoma", onPreviewKeyEvent = {
                if (it.isCtrlPressed && it.key == Key.S && it.type == KeyEventType.KeyDown) {
                    vm.trySend(Save)
                    true
                } else {
                    false
                }
            }) {
                with(LocalDensity.current) {
                    window.minimumSize = Dimension(1240.dp.roundToPx(), 200.dp.roundToPx())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    SnackbarHost(
                        hostState = snackbar,
                        modifier = Modifier.align(Alignment.BottomEnd).zIndex(FOREGROUND)
                    )
                    App(state)
                }
            }
        }
    }
    logger.atInfo().log("Stopping HEMAtoma")
}

@Composable
@Suppress("ModifierMissing") // Is the top-level composable and has no use for modifier
fun App(state: EventState) = MaterialTheme {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Header(state, modifier = Modifier.background(MaterialTheme.colorScheme.primary))
        Row {
            Navigation(state.screen)
            AnimatedContent(state.screen) { current ->
                when (current) {
                    Screen.Configuration -> ConfigurationScreen(state, modifier = Modifier.fillMaxSize())
                    Screen.Competitors -> CompetitorScreen(
                        state.event.competitors,
                        modifier = Modifier.fillMaxSize()
                    )

                    Screen.Scoring -> ScoringScreen(state, modifier = Modifier.fillMaxSize())
                }
            }

        }
    }
}


