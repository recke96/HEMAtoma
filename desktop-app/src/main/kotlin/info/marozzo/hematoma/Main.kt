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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.zIndex
import arrow.continuations.SuspendApp
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.components.*
import info.marozzo.hematoma.contract.ErrorSideEffect
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Screen
import info.marozzo.hematoma.contract.ThrowableSideEffect
import info.marozzo.hematoma.domain.CompetitorName
import info.marozzo.hematoma.domain.RegistrationNumber
import info.marozzo.hematoma.domain.TournamentId
import info.marozzo.hematoma.domain.scoring.Score
import info.marozzo.hematoma.resources.Res
import info.marozzo.hematoma.resources.icon
import org.jetbrains.compose.resources.painterResource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.awt.Dimension

private const val FOREGROUND = 10f
private val logger = FluentLogger.forEnclosingClass()!!

val version = System.getProperty("app.version") ?: "dev"

fun main() = SuspendApp {
    logger.atInfo().log("Start HEMAtoma %s", version)
    awaitApplication {
        val coroutineScope = rememberCoroutineScope()
        val icon = painterResource(Res.drawable.icon)
        val vm = remember { EventViewModel(coroutineScope) }


        Window(
            title = "HEMAtoma",
            icon = icon,
            onPreviewKeyEvent = {
                when (it.key) {
                    Key.S if it.isCtrlPressed && it.isShiftPressed -> true.also { vm.saveAs() }
                    Key.S if it.isCtrlPressed -> true.also { vm.save() }
                    Key.O if it.isCtrlPressed -> true.also { vm.openFile() }
                    else -> false
                }
            },
            onCloseRequest = this::exitApplication,
        ) {
            with(LocalDensity.current) {
                window.minimumSize = Dimension(1240.dp.roundToPx(), 200.dp.roundToPx())
            }

                val snackbar = remember { SnackbarHostState() }
                val state by vm.collectAsState()
                vm.collectSideEffect {
                    when (it) {
                        is ErrorSideEffect -> snackbar.showSnackbar(it.msg, withDismissAction = true)
                        is ThrowableSideEffect -> snackbar.showSnackbar(
                            it.throwable.message ?: "An exception occurred",
                            withDismissAction = true
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    SnackbarHost(
                        hostState = snackbar,
                        modifier = Modifier.align(Alignment.BottomEnd).zIndex(FOREGROUND)
                    )
                    App(
                        state,
                        vm::goto,
                        vm::addCompetitor,
                        vm::addCombat,
                        vm::setWinningThreshold,
                        vm::save,
                        vm::saveAs,
                        vm::openFile,
                    )
            }
        }
    }

    logger.atInfo().log("Stopping HEMAtoma")
}

@Composable
@Suppress("ModifierMissing") // Is the top-level composable and has no use for modifier
fun App(
    state: EventState,
    onNavigate: (Screen) -> Unit,
    onAddCompetitor: (registration: RegistrationNumber, name: CompetitorName) -> Unit,
    onAddCombat: (AddCombatParameters) -> Unit,
    onSetWinningThreshold: (TournamentId, Score) -> Unit,
    onSave: () -> Unit = {},
    onSaveAs: () -> Unit = {},
    onOpen: () -> Unit = {}
) = MaterialTheme {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Header(state, onSave, onSaveAs, onOpen, modifier = Modifier.background(MaterialTheme.colorScheme.primary))
        Row {
            Navigation(state.screen, onNavigate)
            AnimatedContent(state.screen) { current ->
                when (current) {
                    Screen.Configuration -> ConfigurationScreen(
                        state,
                        onSetWinningThreshold,
                        modifier = Modifier.fillMaxSize()
                    )

                    Screen.Competitors -> CompetitorScreen(
                        state.event.competitors,
                        onAddCompetitor,
                        modifier = Modifier.fillMaxSize()
                    )

                    Screen.Scoring -> ScoringScreen(state, onAddCombat, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
