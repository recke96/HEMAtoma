/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.input.event

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
    suspend fun handle(input: AddCompetitor) {
        val event = getCurrentState().event
        event.addCompetitor(input.number, input.name)
            .fold(
                {
                    errs -> flogger.atInfo().log("Validation failed while adding competitor: %s", errs)
                    errs.map(ValidationError::message).map(::ErrorEvent).forEach {
                        postEvent(it)
                    }
                },
                { evt -> updateState { EventState.event.set(it, evt) } }
            )
    }
}
