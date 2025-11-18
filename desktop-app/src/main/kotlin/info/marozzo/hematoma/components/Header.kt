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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import info.marozzo.hematoma.rememberNavigatorEventScreenModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun Header(modifier: Modifier = Modifier) {
    val nav = LocalNavigator.currentOrThrow
    val model = nav.rememberNavigatorEventScreenModel()
    val state by model.collectAsState()

    Row(modifier.heightIn(24.dp, 32.dp).fillMaxWidth()) {
        FileMenu(
            state.path != null,
            model::save,
            model::saveAs,
            model::openFile,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FileMenu(
    hasPath: Boolean,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val dismiss = { setExpanded(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = setExpanded, modifier) {
        Button(
            onClick = { setExpanded(!expanded) },
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        ) {
            Text("File")
        }
        DropdownMenu(expanded, onDismissRequest = dismiss, modifier = Modifier.exposedDropdownSize(false)) {
            DropdownMenuItem(
                text = { Text("Open") },
                trailingIcon = { ShortcutLabel("Ctrl + O") },
                onClick = { dismiss(); onOpen(); },
            )
            DropdownMenuItem(
                text = { Text("Save As") },
                trailingIcon = { ShortcutLabel("Ctrl + Alt + S") },
                onClick = { dismiss(); onSaveAs(); },
            )
            DropdownMenuItem(
                text = { Text("Save") },
                trailingIcon = { ShortcutLabel("Ctrl + S") },
                onClick = { dismiss(); onSave(); },
                enabled = hasPath,
            )
        }
    }
}
