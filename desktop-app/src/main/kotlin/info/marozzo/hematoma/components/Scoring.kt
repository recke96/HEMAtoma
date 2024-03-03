/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import arrow.core.Option
import arrow.core.none
import arrow.core.raise.option
import arrow.core.some
import info.marozzo.hematoma.contract.AddCombat
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.domain.*
import info.marozzo.hematoma.input.AcceptFun


@Composable
fun ScoringScreen(state: EventState, accept: AcceptFun, modifier: Modifier = Modifier) {
    val (tab, setTab) = remember { mutableStateOf(0) }

    Column(modifier) {
        TabRow(selectedTabIndex = tab) {
            Tab(tab == 0, onClick = { setTab(0) }, text = { Text("Record") })
            Tab(tab == 1, onClick = { setTab(1) }, text = { Text("Table") })
        }
        Box(modifier = Modifier.padding(10.dp)) {
            AnimatedContent(tab) {
                when (it) {
                    0 -> CombatRecord(
                        state.event.competitors,
                        state.event.tournaments.single(),
                        accept,
                        modifier = Modifier.fillMaxSize()
                    )

                    1 -> Text("Results table")
                }
            }
        }
    }
}

@Composable
fun CombatRecord(competitors: Competitors, tournament: Tournament, accept: AcceptFun, modifier: Modifier = Modifier) {
    Column(modifier) {
        CombatInput(competitors, tournament, accept)
    }
}

@Composable
fun CombatInput(competitors: Competitors, tournament: Tournament, accept: AcceptFun, modifier: Modifier = Modifier) {
    val competitorsOfTournament = remember(competitors, tournament.registered) {
        competitors.filter { tournament.registered.contains(it.id) }
    }
    val (competitorA, setCompetitorA) = remember { mutableStateOf<Option<Competitor>>(none()) }
    val (competitorB, setCompetitorB) = remember { mutableStateOf<Option<Competitor>>(none()) }
    val (scoreA, setScoreA) = remember { mutableStateOf("") }
    val parsedScoreA = remember(scoreA) { Score(scoreA) }
    val (scoreB, setScoreB) = remember { mutableStateOf("") }
    val parsedScoreB = remember(scoreB) { Score(scoreB) }
    val (doubleHits, setDoubleHits) = remember { mutableStateOf("") }
    val parsedDoubleHits = remember(doubleHits) { Hits(doubleHits) }

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
        Button(onClick = {
            option {
                AddCombat(
                    tournament.id,
                    competitorA.map(Competitor::id).bind(),
                    competitorB.map(Competitor::id).bind(),
                    ignoreErrors { parsedScoreA.bind() },
                    ignoreErrors { parsedScoreB.bind() },
                    ignoreErrors { parsedDoubleHits.bind() }
                )
            }.onSome(accept)
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
