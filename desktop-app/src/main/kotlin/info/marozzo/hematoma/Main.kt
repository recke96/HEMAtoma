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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.zIndex
import arrow.continuations.SuspendApp
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.components.*
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Screen
import info.marozzo.hematoma.shortcuts.handler
import info.marozzo.hematoma.shortcuts.shortcuts
import java.awt.Dimension
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

private const val FOREGROUND = 10f
private val logger = FluentLogger.forEnclosingClass()!!

val version = System.getProperty("app.version") ?: "dev"

fun main() = SuspendApp {
    logger.atInfo().log("Start HEMAtoma %s", version)
    awaitApplication {
        val coroutineScope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }
        val icon = rememberAppIcon()
        val vm = remember(coroutineScope) { EventViewModel(coroutineScope, snackbar) }
        val state by vm.observeStates().collectAsState()

        CompositionLocalProvider(LocalAccept provides vm::trySend) {

            Window(
                title = "HEMAtoma",
                icon = icon,
                onPreviewKeyEvent = shortcuts.handler(vm::trySend),
                onCloseRequest = this::exitApplication,
            ) {
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

@Composable
fun rememberAppIcon(density: Density = LocalDensity.current): Painter? = remember {
    System.getProperty("app.dir")
        ?.let { Paths.get(it, "icon.svg") }
        ?.takeIf { it.exists() }
        ?.inputStream()
        ?.use { loadSvgPainter(it, density) }
}

