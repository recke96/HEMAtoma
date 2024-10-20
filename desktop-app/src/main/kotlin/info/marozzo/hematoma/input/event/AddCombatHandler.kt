/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.event

import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.contract.AddCombat
import info.marozzo.hematoma.contract.ErrorEvent
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.event
import info.marozzo.hematoma.domain.Combat
import info.marozzo.hematoma.input.EventInputHandlerScope

object AddCombatHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    suspend fun EventInputHandlerScope.handle(input: AddCombat) {
        val event = getCurrentState().event
        event.registerCombatForTournament(
            input.tournament,
            Combat(input.competitorA, input.competitorB, input.scoreA, input.scoreB, input.doubleHits)
        ).fold(
            {
                flogger.atInfo().log("Validation failed while registering combat %s", it)
                for (err in it) {
                    postEvent(ErrorEvent(err.message))
                }
            },
            { evt -> updateState { EventState.event.set(it, evt) } }
        )
    }

}
