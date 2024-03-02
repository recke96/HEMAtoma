/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Scoreboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import info.marozzo.hematoma.contract.Goto
import info.marozzo.hematoma.contract.Screen
import info.marozzo.hematoma.input.AcceptFun

@Composable
fun Navigation(screen: Screen, accept: AcceptFun, modifier: Modifier = Modifier) {
    NavigationRail(
        modifier = modifier.widthIn(48.dp, 64.dp).fillMaxHeight(),
        backgroundColor = MaterialTheme.colors.primary
    ) {
        NavigationRailItem(
            screen == Screen.Competitors,
            selectedContentColor = MaterialTheme.colors.secondary,
            unselectedContentColor = MaterialTheme.colors.onPrimary,
            icon = {
                Icon(
                    if (screen == Screen.Competitors) Icons.Filled.Groups else Icons.Outlined.Groups,
                    contentDescription = "Competitors"
                )
            },
            onClick = { accept(Goto(Screen.Competitors)) }
        )
        NavigationRailItem(
            screen == Screen.Scoring,
            selectedContentColor = MaterialTheme.colors.secondary,
            unselectedContentColor = MaterialTheme.colors.onPrimary,
            icon = {
                Icon(
                    if (screen == Screen.Scoring) Icons.Filled.Scoreboard else Icons.Outlined.Scoreboard,
                    contentDescription = "Scoring"
                )
            },
            onClick = { accept(Goto(Screen.Scoring)) }
        )
    }
}
