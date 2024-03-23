/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.event

import androidx.compose.material3.SnackbarHostState
import com.copperleaf.ballast.EventHandler
import info.marozzo.hematoma.EventEventHandlerScope
import info.marozzo.hematoma.FilePickerHostState
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.utils.discard

class EventEventHandler(
    private val snackbar: SnackbarHostState,
    private val filePicker: FilePickerHostState
) : EventHandler<Input, Event, EventState> {
    override suspend fun EventEventHandlerScope.handleEvent(event: Event): Unit = when (event) {
        is RequestFileEvent -> {
            val result = event.toInput(
                filePicker.pickFile(event.title, event.initialDirectory, event.extensions)
            )

            if (result != null) postInput(result)
            else Unit
        }

        is ErrorEvent -> snackbar.showSnackbar(event.msg, withDismissAction = true).discard()
        is ThrowableEvent -> snackbar.showSnackbar(
            event.throwable.message ?: "An exception occurred",
            withDismissAction = true
        ).discard()
    }
}
