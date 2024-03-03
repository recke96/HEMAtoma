/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.DataTable
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

                    1 -> CombatTable(
                        state.event.tournaments.single(),
                        state.event.competitors,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@Composable
fun CombatRecord(competitors: Competitors, tournament: Tournament, accept: AcceptFun, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)) {
        CombatInput(competitors, tournament, accept)
        Divider(thickness = DividerDefaults.Thickness.plus(2.dp))
        CombatRecordList(tournament.record, competitors)
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

@Composable
fun CombatRecordList(combats: Combats, competitors: Competitors, modifier: Modifier = Modifier) {
    val state = rememberLazyListState()
    Box(modifier) {
        LazyColumn(state = state) {
            items(combats) {
                CombatRecordListItem(it, competitors, modifier = Modifier.fillMaxWidth())
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state)
        )
    }
}

@Composable
fun CombatRecordListItem(combat: Combat, competitors: Competitors, modifier: Modifier = Modifier) = Column(modifier) {
    val a = remember(combat.a, competitors) {
        competitors.find { it.id == combat.a }!!
    }
    val b = remember(combat.b, competitors) {
        competitors.find { it.id == combat.b }!!
    }
    ListItem(
        overlineContent = { Text("${a.display()} vs. ${b.display()}", style = MaterialTheme.typography.bodySmall) },
        headlineContent = {
            Text(
                "${combat.scoreA} : ${combat.scoreB} (${combat.doubleHits})",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )
    Divider()
}

@Composable
fun CombatTable(tournament: Tournament, competitors: Competitors, modifier: Modifier = Modifier) {
    val comparator = remember(competitors) {
        compareBy(Result::cut)
            .thenComparing(compareByDescending(Result::doubleHits))
            .thenComparing(Result::wins)
            .thenComparing { r ->
                competitors.find { it.id == r.competitor }?.name?.value!!
            }.reversed()
    }
    val results = remember(tournament) {
        tournament.getResults().values.sortedWith(comparator)
    }

    DataTable(
        modifier = modifier,
        columns = listOf(
            DataColumn(alignment = Alignment.End) {
                Text("Nr.")
            },
            DataColumn(alignment = Alignment.Start) {
                Text("Name")
            },
            DataColumn(alignment = Alignment.End) {
                Text("Scored")
            },
            DataColumn(alignment = Alignment.End) {
                Text("Conceded")
            },
            DataColumn(alignment = Alignment.End) {
                Text("CUT")
            },
            DataColumn(alignment = Alignment.End) {
                Text("Double Hits")
            }
        )
    ) {
        for (result in results) {
            val competitor = competitors.find { it.id == result.competitor }!!
            row {
                cell { Text(competitor.registration.value) }
                cell { Text(competitor.name.value) }
                cell { Text(result.scored.toString()) }
                cell { Text(result.conceded.toString()) }
                cell { Text(result.cut.toString()) }
                cell { Text(result.doubleHits.toString()) }
            }
        }
    }
}

private fun Competitor.display() = "${registration.value}. ${name.value}"
