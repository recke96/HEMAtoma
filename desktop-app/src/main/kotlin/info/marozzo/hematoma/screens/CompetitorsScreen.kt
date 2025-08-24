/*
 * Copyright Jakob Ecker, 2025
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import info.marozzo.hematoma.components.CompetitorNameInput
import info.marozzo.hematoma.components.CompetitorsList
import info.marozzo.hematoma.domain.RegistrationNumber
import info.marozzo.hematoma.rememberNavigatorEventScreenModel
import org.orbitmvi.orbit.compose.collectAsState

class CompetitorsScreen : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val model = nav.rememberNavigatorEventScreenModel()
        val state by model.collectAsState()
        val nextReg = remember(state.event.competitors) {
            RegistrationNumber.Companion(
                state.event.competitors.size.inc().toString()
            ).getOrNull() ?: error("Can't happen")
        }

        Column(modifier = Modifier.fillMaxSize()) {
            CompetitorNameInput(
                number = nextReg,
                onSubmit = { model.addCompetitor(nextReg, it) },
                modifier = Modifier.fillMaxWidth()
            )
            CompetitorsList(state.event.competitors, modifier = Modifier.fillMaxWidth())
        }
    }
}
