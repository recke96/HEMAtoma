/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.serializers

import info.marozzo.hematoma.domain.Competitor
import info.marozzo.hematoma.domain.CompetitorId
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.putAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class CompetitorsSerializer(competitorSerializer: KSerializer<Competitor>) :
    KSerializer<PersistentMap<CompetitorId, Competitor>> {
    private val delegate = ListSerializer(competitorSerializer)

    override val descriptor: SerialDescriptor =
        SerialDescriptor("info.marozzo.hematoma.domain.Competitors", delegate.descriptor)

    override fun deserialize(decoder: Decoder): PersistentMap<CompetitorId, Competitor> =
        persistentMapOf<CompetitorId, Competitor>().putAll(
            delegate.deserialize(decoder).map { it.id to it }
        )

    override fun serialize(encoder: Encoder, value: PersistentMap<CompetitorId, Competitor>) =
        delegate.serialize(encoder, value.values.toList())
}
