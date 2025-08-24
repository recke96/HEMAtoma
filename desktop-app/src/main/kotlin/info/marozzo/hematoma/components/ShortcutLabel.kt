/*
 * Copyright Jakob Ecker, 2025
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ShortcutLabel(text: String, modifier: Modifier = Modifier) = Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.secondaryContainer
) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall
    )
}
