/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.input.event.AddCombatHandler
import info.marozzo.hematoma.input.event.AddCompetitorHandler
import info.marozzo.hematoma.input.event.SetWinningThresholdHandler
import info.marozzo.hematoma.input.file.OpenFileHandler
import info.marozzo.hematoma.input.file.SaveHandler
import info.marozzo.hematoma.input.navigation.GotoHandler

typealias EventInputHandlerScope = InputHandlerScope<Input, Event, EventState>
typealias AcceptFun = (Input) -> Unit

class EventInputHandler : InputHandler<Input, Event, EventState> {
    override suspend fun EventInputHandlerScope.handleInput(input: Input) = when (input) {
        is Goto -> with(GotoHandler) { handle(input) }
        is OpenFile -> with(OpenFileHandler) { handle() }
        is OpenedFile -> with(OpenFileHandler) { handle(input) }
        is Save -> with(SaveHandler) { handleSave() }
        is SaveAs -> with(SaveHandler) { handle() }
        is SavedAt -> with(SaveHandler) { handle(input) }
        is SetWinningThreshold -> with(SetWinningThresholdHandler) { handle(input) }
        is AddCompetitor -> with(AddCompetitorHandler) { handle(input) }
        is AddCombat -> with(AddCombatHandler) { handle(input) }
    }
}
