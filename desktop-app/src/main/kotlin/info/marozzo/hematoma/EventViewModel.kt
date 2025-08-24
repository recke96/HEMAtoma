/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import arrow.core.getOrNone
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.optics.copy
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.withViewModel
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.domain.Combat
import info.marozzo.hematoma.domain.CompetitorId
import info.marozzo.hematoma.domain.CompetitorName
import info.marozzo.hematoma.domain.Event
import info.marozzo.hematoma.domain.RegistrationNumber
import info.marozzo.hematoma.domain.TournamentId
import info.marozzo.hematoma.domain.addCompetitor
import info.marozzo.hematoma.domain.errors.ValidationError
import info.marozzo.hematoma.domain.scoring.Hits
import info.marozzo.hematoma.domain.scoring.Score
import info.marozzo.hematoma.event.EventEventHandler
import info.marozzo.hematoma.input.AcceptFun
import info.marozzo.hematoma.input.EventInputHandler
import info.marozzo.hematoma.utils.FluentLoggingInterceptor
import info.marozzo.hematoma.utils.readFromFile
import info.marozzo.hematoma.utils.writeToFile
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.Syntax
import java.nio.file.StandardOpenOption

typealias SideEffect = info.marozzo.hematoma.contract.Event;
typealias EventEventHandlerScope = EventHandlerScope<Input, SideEffect, EventState>

@Deprecated("Use orbit instead")
class EventViewModel(
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
) : BasicViewModel<Input, SideEffect, EventState>(
    config = BallastViewModelConfiguration.Builder().apply {
        interceptors += FluentLoggingInterceptor<Input, SideEffect, EventState>()
        inputStrategy = FifoInputStrategy()
    }.withViewModel(EventState(), EventInputHandler(), "HEMAtoma").build(),
    eventHandler = EventEventHandler(snackbar),
    coroutineScope = scope
)

@Deprecated("Use orbit instead")
val LocalAccept = staticCompositionLocalOf<AcceptFun> { {} }

class EventViewModel2(scope: CoroutineScope) : ContainerHost<EventState, SideEffect> {

    override val container = scope.container<EventState, SideEffect>(EventState())

    fun goto(screen: Screen) = intent(registerIdling = false) {
        reduce { EventState.screen.set(state, screen) }
    }

    fun openFile() = intent {
        val file = FileKit.pickFile(
            title = "Open Event",
            type = PickerType.File(extensions = listOf("json")),
        )
        val path = file?.file?.toPath() ?: return@intent

        Json.readFromFile<Event>(path).fold({ postSideEffect(ThrowableEvent(it)) }, {
            reduce {
                state.copy {
                    EventState.path set path
                    EventState.event set it
                }
            }
        })
    }

    fun save() = intent {
        val (path, event, _) = state
        if (path == null) {
            postSideEffect(ErrorEvent("No file selected"))
            return@intent
        }

        Json.writeToFile(event, path, StandardOpenOption.TRUNCATE_EXISTING).onLeft {
            postSideEffect(ThrowableEvent(it))
        }
    }

    fun saveAs() = intent {
        val (_, event, _) = state
        val bytes = Json.encodeToString(event).toByteArray()

        val file = FileKit.saveFile(
            baseName = event.name.toString(),
            extension = "json",
            bytes = bytes,
        )

        if (file == null) {
            postSideEffect(ErrorEvent("Failed to save file."))
        } else {
            reduce { EventState.path.set(state, file.file.toPath()) }
        }
    }

    fun addCompetitor(registration: RegistrationNumber, name: CompetitorName) = intent {
        either {
            val orig = state.event
            val added = orig.addCompetitor(registration, name).bind()
            val comp = ensureNotNull(added.competitors.values.find { it.registration == registration }) {
                ValidationError("New competitor not found").nel()
            }

            val tournament = added.tournaments.getOrNone(TournamentId.initial())
                .toEither { ValidationError("Expected a single tournament").nel() }
                .bind()

            added.registerCompetitorForTournament(comp.id, tournament.id).bind()
        }.fold(
            { postErrors(it) },
            { reduce { EventState.event.set(state, it) } }
        )
    }

    fun addCombat(parameters: AddCombatParameters) = intent {
        val (tournamentId, competitorA, competitorB, scoreA, scoreB, doubleHits) = parameters
        state.event.registerCombatForTournament(
            tournamentId, Combat(
                competitorA, competitorB, scoreA, scoreB, doubleHits
            )
        ).fold(
            { postErrors(it) },
            { reduce { EventState.event.set(state, it) } }
        )
    }

    fun setWinningThreshold(tournament: TournamentId, threshold: Score) = intent {
        state.event.setWinningThreshold(tournament, threshold).fold(
            { postErrors(it) },
            { reduce { EventState.event.set(state, it) } }
        )
    }

    suspend fun Syntax<EventState, SideEffect>.postErrors(errors: Iterable<ValidationError>) =
        errors.asSequence()
            .map(ValidationError::message)
            .map(::ErrorEvent)
            .forEach { postSideEffect(it) }
}

data class AddCombatParameters(
    val tournament: TournamentId,
    val competitorA: CompetitorId,
    val competitorB: CompetitorId,
    val scoreA: Score,
    val scoreB: Score,
    val doubleHits: Hits
)
