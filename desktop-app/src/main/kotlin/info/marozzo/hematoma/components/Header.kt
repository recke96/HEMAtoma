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
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import info.marozzo.hematoma.LocalAccept
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Open
import info.marozzo.hematoma.contract.Save
import info.marozzo.hematoma.contract.SaveAs
import java.nio.file.Path

@Composable
fun Header(state: EventState, modifier: Modifier = Modifier) {
    Row(modifier.heightIn(24.dp, 32.dp).fillMaxWidth()) {
        FileMenu(state.event.name.toString(), state.path != null)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FileMenu(name: String, hasPath: Boolean, modifier: Modifier = Modifier) {
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
            OpenMenuItem(onDone = dismiss)
            SaveAsMenuItem(name, onDone = dismiss)
            SaveMenuItem(hasPath, onDone = dismiss)
        }
    }
}

@Composable
private fun OpenMenuItem(modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    val accept = LocalAccept.current
    DropdownMenuItem(
        text = { Text("Open") },
        onClick = { accept(Open); onDone() },
        modifier = modifier
    )
}

@Composable
private fun SaveAsMenuItem(name: String, modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    val accept = LocalAccept.current
    val userDir = remember { System.getProperty("user.home") }
    val (showFilePicker, setShowFilePicker) = remember { mutableStateOf(false) }

    DropdownMenuItem(text = { Text("Save As") }, onClick = { setShowFilePicker(true) }, modifier = modifier)
    DirectoryPicker(showFilePicker, initialDirectory = userDir, title = "Save Event in") { file ->
        setShowFilePicker(false)
        file?.also { accept(SaveAs(Path.of(it, "$name.json"))) }
        onDone()
    }
}

@Composable
private fun SaveMenuItem(hasPath: Boolean, modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    val accept = LocalAccept.current
    DropdownMenuItem(
        text = { Text("Save") },
        onClick = { accept(Save); onDone() },
        enabled = hasPath,
        modifier = modifier
    )
}
