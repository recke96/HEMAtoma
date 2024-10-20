/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.navigation

import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Goto
import info.marozzo.hematoma.contract.screen
import info.marozzo.hematoma.input.EventInputHandlerScope

object GotoHandler {

    suspend fun EventInputHandlerScope.handle(input: Goto) {
        updateState {
            EventState.screen.set(it, input.screen)
        }
    }

}
