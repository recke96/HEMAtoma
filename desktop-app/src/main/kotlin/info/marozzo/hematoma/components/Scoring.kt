/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier


@Composable
fun ScoringScreen(modifier: Modifier = Modifier) {
    val (tab, setTab) = remember { mutableStateOf(0) }

    Column(modifier) {
        TabRow(selectedTabIndex = tab) {
            Tab(tab == 0, onClick = { setTab(0) }, text =  { Text("Record") })
            Tab(tab == 1, onClick = { setTab(1) }, text =  { Text("Table") })
        }
        AnimatedContent(tab) {
            when (it) {
                0 -> Text("Combat records")
                1 -> Text("Results table")
            }
        }
    }
}
