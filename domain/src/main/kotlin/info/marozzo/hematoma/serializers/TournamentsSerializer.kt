/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.serializers

import info.marozzo.hematoma.domain.Tournament
import info.marozzo.hematoma.domain.TournamentId
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
class TournamentsSerializer(tournamentSerializer: KSerializer<Tournament>) :
    KSerializer<PersistentMap<TournamentId, Tournament>> {
    private val delegate = ListSerializer(tournamentSerializer)

    override val descriptor: SerialDescriptor =
        SerialDescriptor("info.marozzo.hematoma.domain.Tournaments", delegate.descriptor)

    override fun deserialize(decoder: Decoder): PersistentMap<TournamentId, Tournament> =
        persistentMapOf<TournamentId, Tournament>().putAll(
            delegate.deserialize(decoder).map { it.id to it }
        )

    override fun serialize(encoder: Encoder, value: PersistentMap<TournamentId, Tournament>) =
        delegate.serialize(encoder, value.values.toList())
}
