/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import info.marozzo.hematoma.domain.Competitor
import info.marozzo.hematoma.domain.Competitors


@Composable
fun CompetitorsList(competitors: Competitors, modifier: Modifier = Modifier) = LazyColumn(modifier) {
    items(competitors, key = { it.id }) {
        CompetitorListItem(it, modifier = Modifier.fillMaxWidth())
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
