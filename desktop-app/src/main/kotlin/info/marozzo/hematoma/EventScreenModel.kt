/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.runtime.Composable
import arrow.core.getOrNone
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.optics.copy
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import info.marozzo.hematoma.contract.*
import info.marozzo.hematoma.domain.*
import info.marozzo.hematoma.domain.errors.ValidationError
import info.marozzo.hematoma.domain.scoring.Hits
import info.marozzo.hematoma.domain.scoring.Score
import info.marozzo.hematoma.utils.readFromFile
import info.marozzo.hematoma.utils.writeToFile
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.serialization.json.Json
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.Syntax
import java.nio.file.StandardOpenOption

@Composable
fun Navigator.rememberNavigatorEventScreenModel() = rememberNavigatorScreenModel { EventScreenModel() }

class EventScreenModel : ContainerHost<EventState, SideEffect>, ScreenModel {

    override val container = screenModelScope.container<EventState, SideEffect>(EventState())

    fun openFile() = intent {
        val file = FileKit.pickFile(
            title = "Open Event",
            type = PickerType.File(extensions = listOf("json")),
        )
        val path = file?.file?.toPath() ?: return@intent

        Json.readFromFile<Event>(path).fold({ postSideEffect(ThrowableSideEffect(it)) }, {
            reduce {
                state.copy {
                    EventState.path set path
                    EventState.event set it
                }
            }
        })
    }

    fun save() = intent {
        val (path, event) = state
        if (path == null) {
            postSideEffect(ErrorSideEffect("No file selected"))
            return@intent
        }

        Json.writeToFile(event, path, StandardOpenOption.TRUNCATE_EXISTING).onLeft {
            postSideEffect(ThrowableSideEffect(it))
        }
    }

    fun saveAs() = intent {
        val (_, event) = state
        val bytes = Json.encodeToString(event).toByteArray()

        val file = FileKit.saveFile(
            baseName = event.name.toString(),
            extension = "json",
            bytes = bytes,
        )

        if (file == null) {
            postSideEffect(ErrorSideEffect("Failed to save file."))
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
        state.event.registerCombatForTournament(
            parameters.tournament,
            Combat(
                parameters.competitorA,
                parameters.competitorB,
                parameters.scoreA,
                parameters.scoreB,
                parameters.doubleHits
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
            .map(::ErrorSideEffect)
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
