/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers

import info.marozzo.hematoma.EventContract
import info.marozzo.hematoma.EventInputHandlerScope
import info.marozzo.hematoma.utils.writeToFile
import kotlinx.serialization.json.Json

suspend fun EventInputHandlerScope.handleSaveAs(input: EventContract.Input.SaveAs): Unit {
    val event = getCurrentState().event
    val result = Json.writeToFile(event, input.path)



}
