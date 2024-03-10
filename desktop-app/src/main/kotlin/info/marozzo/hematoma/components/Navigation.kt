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
import info.marozzo.hematoma.LocalAccept
import info.marozzo.hematoma.contract.Goto
import info.marozzo.hematoma.contract.Screen

@Composable
fun Navigation(screen: Screen, modifier: Modifier = Modifier) {
    val accept = LocalAccept.current
    NavigationRail(
        modifier = modifier.widthIn(48.dp, 64.dp).fillMaxHeight(),
    ) {
        NavigationRailItem(
            selected = screen == Screen.Configuration,
            icon = {
                Icon(
                    if (screen == Screen.Configuration) Icons.Filled.Build else Icons.Outlined.Build,
                    contentDescription = "Configure"
                )
            },
            onClick = { accept(Goto(Screen.Configuration)) }
        )
        NavigationRailItem(
            selected = screen == Screen.Competitors,
            icon = {
                Icon(
                    if (screen == Screen.Competitors) Icons.Filled.Groups else Icons.Outlined.Groups,
                    contentDescription = "Competitors"
                )
            },
            onClick = { accept(Goto(Screen.Competitors)) }
        )
        NavigationRailItem(
            selected = screen == Screen.Scoring,
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
