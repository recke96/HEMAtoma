/*
 * Copyright Jakob Ecker, 2025
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.raise.option
import arrow.core.some
import info.marozzo.hematoma.AddCombatParameters
import info.marozzo.hematoma.domain.Competitor
import info.marozzo.hematoma.domain.CompetitorId
import info.marozzo.hematoma.domain.Tournament
import info.marozzo.hematoma.domain.scoring.Hits
import info.marozzo.hematoma.domain.scoring.Score
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun CombatInput(
    competitors: ImmutableMap<CompetitorId, Competitor>,
    tournament: Tournament,
    onAddCombat: (AddCombatParameters) -> Unit,
    modifier: Modifier = Modifier
) {
    val competitorsOfTournament = remember(competitors, tournament.registered) {
        competitors.filterKeys { tournament.registered.contains(it) }.values
    }
    val (competitorA, setCompetitorA) = remember { mutableStateOf<Option<Competitor>>(none()) }
    val (competitorB, setCompetitorB) = remember { mutableStateOf<Option<Competitor>>(none()) }
    val (scoreA, setScoreA) = remember { mutableStateOf("") }
    val parsedScoreA = remember(scoreA) { Score.parse(scoreA) }
    val (scoreB, setScoreB) = remember { mutableStateOf("") }
    val parsedScoreB = remember(scoreB) { Score.parse(scoreB) }
    val (doubleHits, setDoubleHits) = remember { mutableStateOf("") }
    val parsedDoubleHits = remember(doubleHits) { Hits.parse(doubleHits) }

    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompetitorSelect(
            competitorA,
            competitorsOfTournament,
            onSelect = { setCompetitorA(it.some()) },
            label = { Text("Competitor A") }
        )
        CompetitorSelect(
            competitorB,
            competitorsOfTournament,
            onSelect = { setCompetitorB(it.some()) },
            label = { Text("Competitor B") }
        )
        TextField(
            value = scoreA,
            onValueChange = setScoreA,
            singleLine = true,
            label = { Text("Score A") },
            isError = parsedScoreA.isLeft(),
            modifier = Modifier.widthIn(75.dp, 150.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = scoreB,
            onValueChange = setScoreB,
            singleLine = true,
            label = { Text("Score B") },
            isError = parsedScoreB.isLeft(),
            modifier = Modifier.widthIn(75.dp, 150.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = doubleHits,
            onValueChange = setDoubleHits,
            singleLine = true,
            label = { Text("Double Hits") },
            isError = parsedDoubleHits.isLeft(),
            modifier = Modifier.widthIn(75.dp, 150.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            enabled = competitorA.isSome()
                    && competitorB.isSome()
                    && parsedScoreA.isRight()
                    && parsedScoreB.isRight()
                    && parsedDoubleHits.isRight(),
            onClick = {
                option {
                    AddCombatParameters(
                        tournament.id,
                        competitorA.map(Competitor::id).bind(),
                        competitorB.map(Competitor::id).bind(),
                        ignoreErrors { parsedScoreA.bind() },
                        ignoreErrors { parsedScoreB.bind() },
                        ignoreErrors { parsedDoubleHits.bind() }
                    )
                }.onSome(onAddCombat)
                setCompetitorA(none())
                setCompetitorB(none())
                setScoreA("")
                setScoreB("")
                setDoubleHits("")
            }) {
            Text("Record")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CompetitorSelect(
    selected: Option<Competitor>,
    competitors: Iterable<Competitor>,
    onSelect: (Competitor) -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (input, setInput) = remember(selected) {
        mutableStateOf(selected.map { "${it.registration.value}. ${it.name.value}" }.getOrElse { "" })
    }
    val filtered = remember(input, competitors) {
        competitors.filter {
            it.registration.value.contains(input) || it.name.value.contains(
                input,
                ignoreCase = true
            )
        }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { setExpanded(it) },
    ) {
        TextField(
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable),
            singleLine = true,
            value = input,
            onValueChange = { setInput(it) },
            label = label,
            isError = selected.isNone(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )


        if (filtered.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { setExpanded(false) },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.exposedDropdownSize()
            ) {
                filtered.forEach {
                    DropdownMenuItem(
                        leadingIcon = { Text(it.registration.value) },
                        text = { Text(it.name.value) },
                        onClick = {
                            onSelect(it)
                            setExpanded(false)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}
