/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextTooltip(text: String, modifier: Modifier = Modifier) = Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surfaceContainerHighest,
    shape = RoundedCornerShape(4.dp)
) {
    Text(text, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.labelLarge)
}
