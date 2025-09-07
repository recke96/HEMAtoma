/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import info.marozzo.hematoma.components.Header
import info.marozzo.hematoma.components.Navigation
import info.marozzo.hematoma.contract.ErrorSideEffect
import info.marozzo.hematoma.contract.ThrowableSideEffect
import info.marozzo.hematoma.resources.Res
import info.marozzo.hematoma.resources.icon
import info.marozzo.hematoma.screens.ConfigurationScreen
import org.jetbrains.compose.resources.painterResource
import org.orbitmvi.orbit.compose.collectSideEffect
import org.tinylog.kotlin.Logger
import java.awt.Dimension

val version = lazy { System.getProperty("app.version") ?: "dev" }

fun main() = SuspendApp {
    resourceScope {
        install(
            acquire = { Logger.info("Start HEMAtoma {}", { version.value }) },
            release = { _, _ -> Logger.info("Stop HEMAtoma {}", { version.value }) }
        )
        awaitApplication {
            val icon = painterResource(Res.drawable.icon)

            Window(
                title = "HEMAtoma",
                icon = icon,
                onCloseRequest = this::exitApplication,
            ) {
                val density = LocalDensity.current
                LaunchedEffect(density) {
                    with(density) {
                        window.minimumSize = Dimension(1240.dp.roundToPx(), 200.dp.roundToPx())
                    }
                }
                App()
            }
        }
    }
}

@Composable
@Suppress("ModifierMissing") // Is the top-level composable and has no use for modifier
fun App() {
    val snackbar = remember { SnackbarHostState() }

    MaterialTheme {
        Navigator(ConfigurationScreen()) {
            val model = it.rememberNavigatorEventScreenModel()
            model.collectSideEffect { effect ->
                when (effect) {
                    is ErrorSideEffect -> snackbar.showSnackbar(effect.msg, withDismissAction = true)
                    is ThrowableSideEffect -> snackbar.showSnackbar(
                        effect.throwable.message ?: "An exception occurred",
                        withDismissAction = true
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().onKeyEvent({ evt ->
                when (evt.key) {
                    Key.S if evt.isCtrlPressed && evt.isShiftPressed -> true.also { model.saveAs() }
                    Key.S if evt.isCtrlPressed -> true.also { model.save() }
                    Key.O if evt.isCtrlPressed -> true.also { model.openFile() }
                    else -> false
                }
            })) {
                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Header(modifier = Modifier.background(MaterialTheme.colorScheme.primary))
                    Row {
                        Navigation()
                        CurrentScreen()
                    }
                }
            }
        }
    }
}
