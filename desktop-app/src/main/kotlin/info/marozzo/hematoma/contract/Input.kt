/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.contract

import info.marozzo.hematoma.domain.CompetitorName
import info.marozzo.hematoma.domain.Event
import info.marozzo.hematoma.domain.RegistrationNumber
import java.nio.file.Path

sealed interface Input

// Navigation

data class Goto(val screen: Screen) : Input

// File operations

data class OpenFile(val path: Path) : Input
data class OpenedFile(val path: Path, val event: Event) : Input
data class SaveAs(val path: Path) : Input
data class SavedAs(val path: Path) : Input
data object Save : Input

// Event management

data class AddCompetitor(val registration: RegistrationNumber, val name: CompetitorName) : Input
