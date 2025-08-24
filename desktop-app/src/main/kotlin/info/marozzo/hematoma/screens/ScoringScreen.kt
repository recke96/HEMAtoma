/*
 * Copyright Jakob Ecker, 2025
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import info.marozzo.hematoma.components.CombatRecord
import info.marozzo.hematoma.components.ResultsTable
import info.marozzo.hematoma.rememberNavigatorEventScreenModel
import org.orbitmvi.orbit.compose.collectAsState

class ScoringScreen : Screen {

    private enum class Tab(val text: String) {
        Record("Record"), Table("Table")
    }

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val model = nav.rememberNavigatorEventScreenModel()
        val state by model.collectAsState()
        val (tab, setTab) = remember { mutableStateOf(Tab.Record) }

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = tab.ordinal) {
                Tab.entries.forEach {
                    Tab(selected = tab == it, onClick = { setTab(it) }, text = { Text(it.text) })
                }
            }
            Box(modifier = Modifier.padding(10.dp)) {
                AnimatedContent(tab) {
                    when (it) {
                        Tab.Record -> CombatRecord(
                            state.event.competitors,
                            state.event.tournaments.values.single(),
                            model::addCombat,
                            modifier = Modifier.fillMaxSize()
                        )

                        Tab.Table -> ResultsTable(
                            state.event.tournaments.values.single(),
                            state.event.competitors,
                            model::addCombat,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
