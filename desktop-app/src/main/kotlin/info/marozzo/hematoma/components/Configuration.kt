/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import info.marozzo.hematoma.contract.EventState

@Composable
fun ConfigurationScreen(state: EventState, modifier: Modifier = Modifier) =
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)) {
        TextField(state.event.name.toString(), onValueChange = {}, enabled = false)
        HorizontalDivider(thickness = DividerDefaults.Thickness.plus(2.dp))

    }

