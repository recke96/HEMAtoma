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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import arrow.atomic.Atomic
import arrow.atomic.update
import arrow.collectors.Characteristics
import arrow.collectors.Collector
import arrow.collectors.collect
import arrow.core.Option
import arrow.core.none
import arrow.core.raise.option
import arrow.core.some
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.DataTable
import info.marozzo.hematoma.contract.AddCombat
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.domain.*
import info.marozzo.hematoma.domain.scoring.*
import info.marozzo.hematoma.input.AcceptFun
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap


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
                        state.event.tournaments.values.single(),
                        accept,
                        modifier = Modifier.fillMaxSize()
                    )

                    1 -> ResultsTable(
                        state.event.tournaments.values.single(),
                        state.event.competitors,
                        accept,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@Composable
fun CombatRecord(
    competitors: ImmutableMap<CompetitorId, Competitor>,
    tournament: Tournament,
    accept: AcceptFun,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)) {
        CombatInput(competitors, tournament, accept)
        HorizontalDivider(thickness = DividerDefaults.Thickness.plus(2.dp))
        CombatRecordList(tournament.record, competitors)
    }
}

@Composable
fun CombatInput(
    competitors: ImmutableMap<CompetitorId, Competitor>,
    tournament: Tournament,
    accept: AcceptFun,
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
fun CombatRecordList(
    combats: ImmutableList<Combat>,
    competitors: ImmutableMap<CompetitorId, Competitor>,
    modifier: Modifier = Modifier
) {
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
fun CombatRecordListItem(
    combat: Combat,
    competitors: ImmutableMap<CompetitorId, Competitor>,
    modifier: Modifier = Modifier
) =
    Column(modifier) {
        val a by remember { derivedStateOf { competitors[combat.a]!! } }
        val b by remember { derivedStateOf { competitors[combat.b]!! } }
        ListItem(
            overlineContent = { Text("${a.display()} vs. ${b.display()}", style = MaterialTheme.typography.bodySmall) },
            headlineContent = {
                Text(
                    "${combat.scoreA} : ${combat.scoreB} (${combat.doubleHits})",
                    style = if (combat.doubleHits < Hits.three)
                        MaterialTheme.typography.bodyLarge
                    else MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = TextDecoration.LineThrough
                    )
                )
            }
        )
        HorizontalDivider()
    }

@Composable
fun ResultsTable(
    tournament: Tournament,
    competitors: ImmutableMap<CompetitorId, Competitor>,
    accept: AcceptFun,
    modifier: Modifier = Modifier
) {
    val state = rememberScrollState()
    val results = remember(tournament) { tournament.getResults() }
    val competitorResults = remember(results) {
        results.map { (competitorId, result) ->
            CompetitorResult.from(competitors[competitorId]!!, result)
        }.sortedWith(CompetitorResultComparator)
    }
    val combinedResult = remember(results) {
        results.values.collect(statsCollector)
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)) {
        CombatInput(competitors, tournament, accept)
        HorizontalDivider(thickness = DividerDefaults.Thickness.plus(2.dp))
        Box {
            DataTable(
                modifier = Modifier.fillMaxWidth().verticalScroll(state),
                columns = listOf(
                    DataColumn(alignment = Alignment.End) {
                        Text("Nr.", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.Start) {
                        Text("Name", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.End) {
                        Text("Scored", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.End) {
                        Text("Conceded", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.End) {
                        Text("CUT", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.End) {
                        Text("Double Hits", fontWeight = FontWeight.Bold)
                    }
                )
            ) {
                for (result in competitorResults) {
                    row {
                        cell { Text(result.registration.value) }
                        cell { Text(result.name.value) }
                        cell { Text(result.scored.toString()) }
                        cell { Text(result.conceded.toString()) }
                        cell { Text(result.cut.toString()) }
                        cell { Text(result.doubleHits.toString()) }
                    }
                }
                row {
                    with(combinedResult) {
                        cell { }
                        cell { Text("Summary", fontWeight = FontWeight.Bold) }
                        cell {
                            Text(
                                "Tot. $scored",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        cell {
                            Text(
                                "Tot. $conceded",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        cell {

                        }
                        cell {
                            Text(
                                "Tot. ${doubleHits.half()}",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(state)
            )
        }
    }
}

private data class CompetitorResult(
    val registration: RegistrationNumber,
    val name: CompetitorName,
    val matches: Matches,
    val wins: Matches,
    val losses: Matches,
    val scored: Score,
    val conceded: Score,
    val cut: CUT,
    val doubleHits: Hits
) {
    companion object {
        fun from(competitor: Competitor, result: Result) = CompetitorResult(
            competitor.registration,
            competitor.name,
            result.matches,
            result.wins,
            result.losses,
            result.scored,
            result.conceded,
            result.cut,
            result.doubleHits
        )
    }

}


val statsCollector = Collector.nonSuspendOf<Atomic<Result>, Result, Result>(
    supply = { Atomic(Result.empty) },
    accumulate = { acc, value -> acc.update { it + value } },
    finish = { it.get() },
    characteristics = Characteristics.CONCURRENT_UNORDERED
)

private val CompetitorResultComparator = compareByDescending(CompetitorResult::cut)
    .thenComparing(CompetitorResult::doubleHits)
    .thenComparing(compareByDescending(CompetitorResult::wins))
    .thenComparing(compareBy { it.name.value })

fun Competitor.display() = "${registration.value}. ${name.value}"
