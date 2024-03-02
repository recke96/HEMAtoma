/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.inputhandlers.event

import arrow.core.getOrElse
import com.google.common.flogger.FluentLogger
import info.marozzo.hematoma.EventInputHandlerScope
import info.marozzo.hematoma.contract.AddCompetitor
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.event

object AddCompetitorHandler {

    private val flogger = FluentLogger.forEnclosingClass()!!

    context(EventInputHandlerScope)
    suspend fun handle(input: AddCompetitor) {
        updateState {
            EventState.event.modify(it) { evt ->
                evt.addCompetitor(input.number, input.name)
                    .onLeft { err -> flogger.atInfo().log("Validation failed while adding competitor: %s", err) }
                    .getOrElse { evt }
            }
        }
    }
}
