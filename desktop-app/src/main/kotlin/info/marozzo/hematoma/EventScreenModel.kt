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
import info.marozzo.hematoma.utils.withIntent
import info.marozzo.hematoma.utils.writeToFile
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.pickFile
import kotlinx.serialization.json.Json
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.Syntax
import org.tinylog.kotlin.Logger
import java.nio.file.StandardOpenOption

@Composable
fun Navigator.rememberNavigatorEventScreenModel() = rememberNavigatorScreenModel { EventScreenModel() }

class EventScreenModel : ContainerHost<EventState, SideEffect>, ScreenModel {

    override val container = screenModelScope.container<EventState, SideEffect>(EventState())

    fun openFile() = intent {
        withIntent("openFile") {

            val file = FileKit.pickFile(
                title = "Open Event",
                type = PickerType.File(extensions = listOf("json")),
            )
            val path = file?.file?.toPath()
            if (path == null) {
                Logger.debug("No file selected")
                return@withIntent
            }

            Json.readFromFile<Event>(path).fold(
                {
                    Logger.warn(it, "Failed to open file {}", path)
                    postSideEffect(ThrowableSideEffect(it))
                },
                {
                    reduce {
                        state.copy {
                            EventState.path set path
                            EventState.event set it
                        }
                    }
                })
        }
    }

    fun save() = intent {
        withIntent("save") {

            val (path, event) = state
            if (path == null) {
                Logger.debug("No file selected")
                postSideEffect(ErrorSideEffect("No file selected"))
                return@withIntent
            }

            Json.writeToFile(event, path, StandardOpenOption.TRUNCATE_EXISTING).onLeft {
                Logger.warn(it, "Failed saving to file {}", path)
                postSideEffect(ThrowableSideEffect(it))
            }
        }
    }

    fun saveAs() = intent {
        withIntent("saveAs") {
            val (_, event) = state

            val file = FileKit.saveFile(
                baseName = event.name.toString(),
                extension = "json"
            )?.file?.toPath()

            if (file == null) {
                Logger.warn("No file selected")
                return@withIntent
            }

            Json.writeToFile(event, file).fold(
                {
                    Logger.warn(it, "Failed saving to file {}", file)
                    postSideEffect(ThrowableSideEffect(it))
                },
                { reduce { EventState.path.set(state, file) } })
        }
    }

    fun addCompetitor(registration: RegistrationNumber, name: CompetitorName) = intent {
        withIntent("addCompetitor") {
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
                { postAndLogErrors(it) },
                { reduce { EventState.event.set(state, it) } }
            )
        }
    }

    fun addCombat(parameters: AddCombatParameters) = intent {
        withIntent("addCombat") {
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
                { postAndLogErrors(it) },
                { reduce { EventState.event.set(state, it) } }
            )
        }
    }

    fun setWinningThreshold(tournament: TournamentId, threshold: Score) = intent {
        withIntent("setWinningThreshold") {
            state.event.setWinningThreshold(tournament, threshold).fold(
                { postAndLogErrors(it) },
                { reduce { EventState.event.set(state, it) } }
            )
        }
    }

    suspend fun Syntax<EventState, SideEffect>.postAndLogErrors(errors: Iterable<ValidationError>) {
        Logger.debug("Validation errors:\n\t{}", { errors.joinToString("\n\t") })
        errors.asSequence()
            .map(ValidationError::message)
            .map(::ErrorSideEffect)
            .forEach { postSideEffect(it) }
    }
}

data class AddCombatParameters(
    val tournament: TournamentId,
    val competitorA: CompetitorId,
    val competitorB: CompetitorId,
    val scoreA: Score,
    val scoreB: Score,
    val doubleHits: Hits
)
