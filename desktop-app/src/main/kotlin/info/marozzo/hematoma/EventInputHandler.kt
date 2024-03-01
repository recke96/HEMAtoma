/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import info.marozzo.hematoma.inputhandlers.OpenFileHandler
import info.marozzo.hematoma.inputhandlers.SaveAsHandler
import info.marozzo.hematoma.inputhandlers.SaveHandler


typealias EventInputHandlerScope = InputHandlerScope<EventContract.Input, Nothing, EventContract.State>
typealias AcceptFun = (EventContract.Input) -> Unit

class EventInputHandler : InputHandler<EventContract.Input, Nothing, EventContract.State> {
    override suspend fun EventInputHandlerScope.handleInput(input: EventContract.Input) {
        when (input) {
            is EventContract.Input.OpenFile -> OpenFileHandler.handle(input)
            is EventContract.Input.OpenedFile -> OpenFileHandler.handle(input)
            is EventContract.Input.SaveAs -> SaveAsHandler.handle(input)
            is EventContract.Input.SavedAs -> SaveAsHandler.handle(input)
            is EventContract.Input.Save -> SaveHandler.handle()
        }
    }
}
