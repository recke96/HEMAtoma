/*
 * Copyright Jakob Ecker, 2025
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import info.marozzo.hematoma.AddCombatParameters
import info.marozzo.hematoma.domain.Combat
import info.marozzo.hematoma.domain.Competitor
import info.marozzo.hematoma.domain.CompetitorId
import info.marozzo.hematoma.domain.Tournament
import info.marozzo.hematoma.domain.scoring.ScoringSettings
import info.marozzo.hematoma.utils.display
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun CombatRecord(
    competitors: ImmutableMap<CompetitorId, Competitor>,
    tournament: Tournament,
    onAddCombat: (AddCombatParameters) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)) {
        CombatInput(competitors, tournament, onAddCombat)
        HorizontalDivider(thickness = DividerDefaults.Thickness.plus(2.dp))
        CombatRecordList(tournament.record, tournament.scoringSettings, competitors)
    }
}


@Composable
private fun CombatRecordList(
    combats: ImmutableList<Combat>,
    settings: ScoringSettings,
    competitors: ImmutableMap<CompetitorId, Competitor>,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()
    Box(modifier) {
        LazyColumn(state = state) {
            items(combats) {
                CombatRecordListItem(it, competitors, settings, modifier = Modifier.fillMaxWidth())
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state)
        )
    }
}

@Composable
private fun CombatRecordListItem(
    combat: Combat,
    competitors: ImmutableMap<CompetitorId, Competitor>,
    settings: ScoringSettings,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        val a by remember { derivedStateOf { competitors[combat.a]!! } }
        val b by remember { derivedStateOf { competitors[combat.b]!! } }
        ListItem(
            overlineContent = { Text("${a.display()} vs. ${b.display()}", style = MaterialTheme.typography.bodySmall) },
            headlineContent = {
                Text(
                    "${combat.scoreA} : ${combat.scoreB} (${combat.doubleHits})",
                    style = if (settings.isPunished(combat)) {
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }
                )
            }
        )
        HorizontalDivider()
    }
}
