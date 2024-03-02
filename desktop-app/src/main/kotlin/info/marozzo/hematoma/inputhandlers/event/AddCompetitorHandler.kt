/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers.event

import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.EventInputHandlerScope
import info.marozzo.hematoma.contract.AddCompetitor
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.event
import info.marozzo.hematoma.domain.CompetitorName
import info.marozzo.hematoma.domain.RegistrationNumber
import info.marozzo.hematoma.domain.errors.ValidationError

object AddCompetitorHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle(input: AddCompetitor) {
        either<NonEmptyList<ValidationError>, Unit> {
            zipOrAccumulate(
                { RegistrationNumber(input.number).bind() },
                { CompetitorName(input.name).bind() }
            ) { reg, name ->
                updateState {
                    EventState.event.modify(it) { evt ->
                        evt.addCompetitor(reg, name).bind()
                    }
                }
            }
        }.onLeft { flogger.atInfo().log("Validation errors for adding a competitor: %s", it) }
    }
}
