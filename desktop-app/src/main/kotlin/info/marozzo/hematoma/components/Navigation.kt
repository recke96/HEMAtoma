/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Scoreboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import info.marozzo.hematoma.screens.CompetitorsScreen
import info.marozzo.hematoma.screens.ConfigurationScreen
import info.marozzo.hematoma.screens.ScoringScreen

@Composable
fun Navigation(modifier: Modifier = Modifier) {
    val nav = LocalNavigator.currentOrThrow

    NavigationRail(
        modifier = modifier.widthIn(48.dp, 64.dp).fillMaxHeight(),
    ) {
        NavigationRailItem(
            selected = nav.lastItem is ConfigurationScreen,
            icon = {
                Icon(
                    if (nav.lastItem is ConfigurationScreen) Icons.Filled.Build else Icons.Outlined.Build,
                    contentDescription = "Configure"
                )
            },
            onClick = { nav.replaceAll(ConfigurationScreen()) }
        )
        NavigationRailItem(
            selected = nav.lastItem is CompetitorsScreen,
            icon = {
                Icon(
                    if (nav.lastItem is CompetitorsScreen) Icons.Filled.Groups else Icons.Outlined.Groups,
                    contentDescription = "Competitors"
                )
            },
            onClick = { nav.replaceAll(CompetitorsScreen()) }
        )
        NavigationRailItem(
            selected = nav.lastItem is ScoringScreen,
            icon = {
                Icon(
                    if (nav.lastItem is ScoringScreen) Icons.Filled.Scoreboard else Icons.Outlined.Scoreboard,
                    contentDescription = "Scoring"
                )
            },
            onClick = { nav.replaceAll(ScoringScreen()) }
        )
    }
}
