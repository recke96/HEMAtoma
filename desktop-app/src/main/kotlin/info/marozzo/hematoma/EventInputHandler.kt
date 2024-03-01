/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import info.marozzo.hematoma.inputhandlers.handleOpenFile
import info.marozzo.hematoma.inputhandlers.handleSave
import info.marozzo.hematoma.inputhandlers.handleSaveAs


typealias EventInputHandlerScope = InputHandlerScope<EventContract.Input, Nothing, EventContract.State>
typealias AcceptFun = (EventContract.Input) -> Unit

class EventInputHandler : InputHandler<EventContract.Input, Nothing, EventContract.State> {
    override suspend fun EventInputHandlerScope.handleInput(input: EventContract.Input) {
        when (input) {
            is EventContract.Input.OpenFile -> handleOpenFile(input)
            is EventContract.Input.SaveAs -> handleSaveAs(input)
            is EventContract.Input.Save -> handleSave()
        }
    }
}
