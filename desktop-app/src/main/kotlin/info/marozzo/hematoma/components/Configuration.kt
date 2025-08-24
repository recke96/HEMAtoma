/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.domain.Tournament
import info.marozzo.hematoma.domain.TournamentId
import info.marozzo.hematoma.domain.scoring.FiorDellaSpadaScoring
import info.marozzo.hematoma.domain.scoring.Score
import info.marozzo.hematoma.domain.scoring.ScoringSettings

@Composable
fun ConfigurationScreen(
    state: EventState,
    onSetWinningThreshold: (TournamentId, Score) -> Unit,
    modifier: Modifier = Modifier
) =
    Column(modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)) {
        TextField(state.event.name.toString(), onValueChange = {}, enabled = false)
        HorizontalDivider(thickness = DividerDefaults.Thickness.plus(2.dp))
        LazyColumn {
            items(state.event.tournaments.values.toList()) {
                TournamentConfigListItem(
                    it,
                    onSetWinningThreshold,
                    expandedByDefault = state.event.tournaments.keys.first() == it.id
                )
                HorizontalDivider()
            }
        }
    }

private object Rotation {
    const val EXPANDED = 180f
    const val CLOSED = -90f
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TournamentConfigListItem(
    tournament: Tournament,
    onSetWinningThreshold: (TournamentId, Score) -> Unit,
    modifier: Modifier = Modifier,
    expandedByDefault: Boolean = false,
) {
    val (expanded, setExpanded) = remember(expandedByDefault) { mutableStateOf(expandedByDefault) }

    Column(modifier) {
        ListItem(
            headlineContent = { Text("${tournament.name} Settings") },
            trailingContent = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Expand Tournament Settings",
                    modifier = Modifier.rotate(if (expanded) Rotation.EXPANDED else Rotation.CLOSED)
                )
            },
            modifier = Modifier.onClick { setExpanded(!expanded) }
        )
        AnimatedVisibility(expanded) {
            Surface {
                ScoringConfig(
                    tournament.id,
                    tournament.scoringSettings,
                    onSetWinningThreshold,
                    modifier = Modifier.padding(start = 20.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ScoringConfig(
    tournamentId: TournamentId,
    settings: ScoringSettings,
    onSetWinningThreshold: (TournamentId, Score) -> Unit,
    modifier: Modifier = Modifier
) =
    when (settings) {
        is FiorDellaSpadaScoring -> FiorDellaSpadaScoringConfig(tournamentId, settings, onSetWinningThreshold, modifier)
    }

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun FiorDellaSpadaScoringConfig(
    tournamentId: TournamentId,
    settings: FiorDellaSpadaScoring,
    onSetWinningThreshold: (TournamentId, Score) -> Unit,
    modifier: Modifier = Modifier
) {
    val (input, setInput) = remember(settings.winningThreshold) { mutableStateOf(settings.winningThreshold.toString()) }
    val parsedInput = remember(input) { Score.parse(input) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)) {
        Text("Scoring", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start)) {
            TooltipArea(
                tooltip = {
                    TextTooltip("When a competitor reaches or exceeds this score, the combat is finished")
                }
            ) {
                TextField(
                    input,
                    onValueChange = setInput,
                    isError = parsedInput.isLeft(),
                    label = { Text("Winning Threshold") },
                    placeholder = { Text("Score") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions {
                        onSetWinningThreshold(tournamentId, parsedInput.getOrNull()!!)
                    },
                    modifier = Modifier.onFocusChanged {
                        if (!it.isFocused) parsedInput.onRight { thresh ->
                            onSetWinningThreshold(tournamentId, thresh)
                        } else Unit
                    }
                )
            }
            TooltipArea(
                tooltip = {
                    TextTooltip(
                        "When a combat exceeds this amount of double hits," +
                                " it is ended with a loosing score for both participants"
                    )
                }
            ) {
                TextField(
                    settings.doubleHitThreshold.toString(),
                    onValueChange = {},
                    enabled = false,
                    singleLine = true,
                    label = { Text("Double-Hit Threshold") }
                )
            }
        }
    }
}
