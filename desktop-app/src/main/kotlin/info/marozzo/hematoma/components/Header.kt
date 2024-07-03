/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import info.marozzo.hematoma.LocalAccept
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Save
import info.marozzo.hematoma.shortcuts.ShortcutLabel

@Composable
fun Header(state: EventState, modifier: Modifier = Modifier) {
    Row(modifier.heightIn(24.dp, 32.dp).fillMaxWidth()) {
        FileMenu(state.path != null)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FileMenu(hasPath: Boolean, modifier: Modifier = Modifier) {
    val accept = LocalAccept.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val dismiss = { setExpanded(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = setExpanded, modifier) {
        Button(
            onClick = { setExpanded(!expanded) },
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.menuAnchor(),
        ) {
            Text("File")
        }
        DropdownMenu(expanded, onDismissRequest = dismiss, modifier = Modifier.exposedDropdownSize(false)) {
            DropdownMenuItem(
                text = { Text("Open") },
                trailingIcon = { ShortcutLabel("Ctrl + O") },
                onClick = { dismiss(); },
            )
            DropdownMenuItem(
                text = { Text("Save As") },
                trailingIcon = { ShortcutLabel("Ctrl + Alt + S") },
                onClick = { dismiss(); },
            )
            DropdownMenuItem(
                text = { Text("Save") },
                trailingIcon = { ShortcutLabel("Ctrl + S") },
                onClick = { dismiss(); accept(Save) },
                enabled = hasPath,
            )
        }
    }
}
