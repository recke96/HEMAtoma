/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.OpenFile
import info.marozzo.hematoma.contract.Save
import info.marozzo.hematoma.contract.SaveAs
import info.marozzo.hematoma.input.AcceptFun
import java.nio.file.Path

@Composable
fun Header(state: EventState, accept: AcceptFun, modifier: Modifier = Modifier) {
    Row(modifier.heightIn(24.dp, 32.dp).fillMaxWidth()) {
        FileMenu(state.event.name, state.path != null, accept)
    }
}

@Composable
private fun FileMenu(name: String, hasPath: Boolean, accept: AcceptFun, modifier: Modifier = Modifier) {
    val (isOpen, setIsOpen) = remember { mutableStateOf(false) }
    val dismiss = { setIsOpen(false) }
    Box(modifier) {
        Button(
            onClick = { setIsOpen(!isOpen) },
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant)
        ) {
            Text("File")
        }
        DropdownMenu(isOpen, onDismissRequest = dismiss) {
            OpenMenuItem(accept, onDone = dismiss)
            SaveAsMenuItem(name, accept, onDone = dismiss)
            SaveMenuItem(hasPath, accept, onDone = dismiss)
        }
    }
}

@Composable
private fun OpenMenuItem(accept: AcceptFun, modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    val userDir = remember { System.getProperty("user.home") }
    val (showFilePicker, setShowFilePicker) = remember { mutableStateOf(false) }

    DropdownMenuItem(onClick = { setShowFilePicker(true) }, modifier = modifier) {
        Text("Open")
    }
    FilePicker(
        showFilePicker,
        initialDirectory = userDir,
        fileExtensions = listOf("json"),
        title = "Open Event-File"
    ) { file ->
        setShowFilePicker(false)
        file?.also { accept(OpenFile(Path.of(it.path))) }
        onDone()
    }
}

@Composable
private fun SaveAsMenuItem(name: String, accept: AcceptFun, modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    val userDir = remember { System.getProperty("user.home") }
    val (showFilePicker, setShowFilePicker) = remember { mutableStateOf(false) }

    DropdownMenuItem(onClick = { setShowFilePicker(true) }, modifier = modifier) {
        Text("Save As")
    }
    DirectoryPicker(showFilePicker, initialDirectory = userDir, title = "Save Event in") { file ->
        setShowFilePicker(false)
        file?.also { accept(SaveAs(Path.of(it, "$name.json"))) }
        onDone()
    }
}

@Composable
private fun SaveMenuItem(hasPath: Boolean, accept: AcceptFun, modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    DropdownMenuItem(onClick = { accept(Save); onDone() }, enabled = hasPath, modifier = modifier) {
        Text("Save")
    }
}
