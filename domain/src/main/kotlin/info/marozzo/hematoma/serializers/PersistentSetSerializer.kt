/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.serializers

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class PersistentSetSerializer<E>(elementSerializer: KSerializer<E>) : KSerializer<PersistentSet<E>> {
    private val delegate = SetSerializer(elementSerializer)

    override val descriptor: SerialDescriptor =
        SerialDescriptor("kotlinx.collections.immutable.PersistentSet", delegate.descriptor)

    override fun deserialize(decoder: Decoder): PersistentSet<E> = delegate.deserialize(decoder).toPersistentSet()
    override fun serialize(encoder: Encoder, value: PersistentSet<E>) = delegate.serialize(encoder, value)
}
