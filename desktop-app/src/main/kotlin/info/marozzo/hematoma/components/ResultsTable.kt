/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import arrow.atomic.Atomic
import arrow.atomic.update
import arrow.collectors.Characteristics
import arrow.collectors.Collector
import arrow.collectors.collect
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.material3.DataTable
import info.marozzo.hematoma.AddCombatParameters
import info.marozzo.hematoma.domain.*
import info.marozzo.hematoma.domain.scoring.*
import kotlinx.collections.immutable.ImmutableMap


@Composable
fun ResultsTable(
    tournament: Tournament,
    competitors: ImmutableMap<CompetitorId, Competitor>,
    onAddCombat: (AddCombatParameters) -> Unit,
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
        CombatInput(competitors, tournament, onAddCombat)
        HorizontalDivider(thickness = DividerDefaults.Thickness.plus(2.dp))
        Box {
            DataTable(
                modifier = Modifier.fillMaxWidth().verticalScroll(state),
                columns = listOf(
                    DataColumn(alignment = Alignment.CenterEnd) {
                        Text("Nr.", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.CenterStart) {
                        Text("Name", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.CenterEnd) {
                        Text("Scored", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.CenterEnd) {
                        Text("Conceded", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.CenterEnd) {
                        Text("CUT", fontWeight = FontWeight.Bold)
                    },
                    DataColumn(alignment = Alignment.CenterEnd) {
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


private val statsCollector = Collector.nonSuspendOf<Atomic<Result>, Result, Result>(
    supply = { Atomic(EmptyResult) },
    accumulate = { acc, value -> acc.update { it + value } },
    finish = { it.get() },
    characteristics = Characteristics.CONCURRENT_UNORDERED
)

private val CompetitorResultComparator = compareByDescending(CompetitorResult::cut)
    .thenComparing(CompetitorResult::doubleHits)
    .thenComparing(compareByDescending(CompetitorResult::wins))
    .thenComparing(compareBy { it.name.value })
