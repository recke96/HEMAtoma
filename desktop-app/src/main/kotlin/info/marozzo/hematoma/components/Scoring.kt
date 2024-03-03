/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupProperties
import info.marozzo.hematoma.domain.Competitor
import info.marozzo.hematoma.domain.Competitors


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CompetitorSelect(
    selected: Competitor?,
    competitors: Competitors,
    onSelect: (Competitor) -> Unit,
    modifier: Modifier = Modifier
) {

    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (input, setInput) = remember(selected) {
        mutableStateOf(selected?.let { "${it.registration.value}. ${it.name.value}" } ?: "")
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
            modifier = Modifier.menuAnchor(),
            singleLine = true,
            value = input,
            onValueChange = { setInput(it) },
            label = { Text("Competitor") },
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
