/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.serializers

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class PersistentListSerializer<E>(elementSerializer: KSerializer<E>) : KSerializer<PersistentList<E>> {
    private val delegate = ListSerializer(elementSerializer)

    override val descriptor: SerialDescriptor =
        SerialDescriptor("kotlinx.collections.immutable.PersistentSet", delegate.descriptor)

    override fun deserialize(decoder: Decoder): PersistentList<E> = delegate.deserialize(decoder).toPersistentList()
    override fun serialize(encoder: Encoder, value: PersistentList<E>) = delegate.serialize(encoder, value)
}
