/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.event

import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.contract.AddCompetitor
import info.marozzo.hematoma.contract.ErrorEvent
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.event
import info.marozzo.hematoma.domain.errors.ValidationError
import info.marozzo.hematoma.input.EventInputHandlerScope

object AddCompetitorHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle(input: AddCompetitor) =
        either {
            val original = getCurrentState().event
            val added = original.addCompetitor(input.registration, input.name).bind()
            val newComp = ensureNotNull(added.competitors.find { it.registration == input.registration }) {
                ValidationError("New competitor not found", "input").nel()
            }
            // Currently we work with a single tournament, so we add every competitor to this tournament
            val tournament = ensureNotNull(original.tournaments.firstOrNull()) {
                ValidationError("Exected a single tournament", "input").nel()
            }
            added.registerCompetitorForTournament(newComp.id, tournament.id).bind()
        }.fold(
            { errs ->
                flogger.atInfo().log("Validation failed while adding competitor: %s", errs)
                errs.map(ValidationError::message).map(::ErrorEvent).forEach {
                    postEvent(it)
                }
            },
            { evt -> updateState { EventState.event.set(it, evt) } }
        )
}
