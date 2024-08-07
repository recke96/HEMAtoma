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
        is Goto -> GotoHandler.handle(input)
        is OpenFile -> OpenFileHandler.handle()
        is OpenedFile -> OpenFileHandler.handle(input)
        is Save -> SaveHandler.handleSave()
        is SaveAs -> SaveHandler.handle()
        is SavedAt -> SaveHandler.handle(input)
        is SetWinningThreshold -> SetWinningThresholdHandler.handle(input)
        is AddCompetitor -> AddCompetitorHandler.handle(input)
        is AddCombat -> AddCombatHandler.handle(input)
    }
}
