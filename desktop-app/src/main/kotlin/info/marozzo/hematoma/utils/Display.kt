/*
 * Copyright Jakob Ecker, 2025
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.utils

import info.marozzo.hematoma.domain.Competitor

fun Competitor.display() = "${registration.value}. ${name.value}"
