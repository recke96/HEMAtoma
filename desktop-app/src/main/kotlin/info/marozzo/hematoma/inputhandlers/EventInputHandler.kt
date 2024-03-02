/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers

import com.copperleaf.ballast.InputHandler
import info.marozzo.hematoma.EventInputHandlerScope
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.inputhandlers.file.OpenFileHandler
import info.marozzo.hematoma.inputhandlers.file.SaveAsHandler
import info.marozzo.hematoma.inputhandlers.file.SaveHandler

class EventInputHandler : InputHandler<Input, Nothing, EventState> {
    override suspend fun EventInputHandlerScope.handleInput(input: Input) = when (input) {
        is OpenFile -> OpenFileHandler.handle(input)
        is OpenedFile -> OpenFileHandler.handle(input)
        is SaveAs -> SaveAsHandler.handle(input)
        is SavedAs -> SaveAsHandler.handle(input)
        is Save -> SaveHandler.handle()
    }
}
