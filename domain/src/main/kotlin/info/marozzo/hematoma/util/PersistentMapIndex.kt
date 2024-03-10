/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.util

import arrow.core.getOrNone
import arrow.optics.Optional
import arrow.optics.typeclasses.Index
import kotlinx.collections.immutable.PersistentMap


fun <K, V> Index.Companion.persistentMap(): Index<PersistentMap<K, V>, K, V> = Index { i ->
    Optional(
        getOrModify = { it.getOrNone(i).toEither { it } },
        set = { m, v -> m.put(i, v) }
    )
}
