/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.contract

import info.marozzo.hematoma.domain.*
import info.marozzo.hematoma.domain.Event
import info.marozzo.hematoma.domain.scoring.Hits
import info.marozzo.hematoma.domain.scoring.Score
import java.nio.file.Path

sealed interface Input

// Navigation

data class Goto(val screen: Screen) : Input

// File operations

data object OpenFile : Input
data class OpenedFile(val path: Path, val event: Event) : Input
data object Save : Input
data object SaveAs : Input
data class SavedAt(val path: Path) : Input

// Event management

data class SetWinningThreshold(val tournament: TournamentId, val threshold: Score): Input
data class AddCompetitor(val registration: RegistrationNumber, val name: CompetitorName) : Input
data class AddCombat(
    val tournament: TournamentId,
    val competitorA: CompetitorId,
    val competitorB: CompetitorId,
    val scoreA: Score,
    val scoreB: Score,
    val doubleHits: Hits
) : Input
