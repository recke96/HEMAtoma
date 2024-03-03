/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.TextField
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import info.marozzo.hematoma.contract.AddCompetitor
import info.marozzo.hematoma.domain.Competitor
import info.marozzo.hematoma.domain.CompetitorName
import info.marozzo.hematoma.domain.Competitors
import info.marozzo.hematoma.domain.RegistrationNumber
import info.marozzo.hematoma.input.AcceptFun

@Composable
fun CompetitorSection(competitors: Competitors, accept: AcceptFun, modifier: Modifier = Modifier) = Column(modifier) {
    val nextReg = remember(competitors) {
        RegistrationNumber(
            competitors.size.inc().toString()
        ).getOrNull() ?: error("Can't happen")
    }
    CompetitorNameInput(
        number = nextReg,
        onSubmit = { accept(AddCompetitor(nextReg, it)) },
        modifier = Modifier.fillMaxWidth()
    )
    CompetitorsList(competitors, modifier = Modifier.fillMaxWidth())
}

@Composable
fun CompetitorsList(competitors: Competitors, modifier: Modifier = Modifier) {
    val state = rememberLazyListState()
    Box(modifier) {
        LazyColumn(state = state) {
            items(competitors, key = { it.id }) {
                CompetitorListItem(it, modifier = Modifier.fillMaxWidth())
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun CompetitorListItem(competitor: Competitor, modifier: Modifier = Modifier) = Column(modifier) {
    ListItem(icon = { Text("${competitor.registration.value}.") }) {
        Text(competitor.name.value)
    }
    Divider()
}

@Composable
fun CompetitorNameInput(number: RegistrationNumber, onSubmit: (CompetitorName) -> Unit, modifier: Modifier = Modifier) {
    val (input, setInput) = remember { mutableStateOf("") }
    val parsedInput = remember(input) { CompetitorName(input) }

    TextField(
        value = input,
        onValueChange = { setInput(it) },
        label = { Text("Add Competitor") },
        placeholder = { Text("Name") },
        leadingIcon = { Text("${number.value}.") },
        isError = parsedInput.isLeft(),
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions {
            parsedInput
                .onRight(onSubmit)
                .onRight { setInput("") }
        }
    )
}