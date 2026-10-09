package world.respect.shared.viewmodel.catalog

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * When a user is in pick mode on an OpdsFeed they might be looking for a publication or a feed.
 */
@Serializable(with = PickTypeSerializer::class)
enum class OpdsPickType(val value :String) {

    CATALOG_FEED("feed"), PUBLICATION("publication");

    companion object {

        fun fromValue(value: String): OpdsPickType {
            return entries.first { it.value == value }
        }

    }
}

object PickTypeSerializer: KSerializer<OpdsPickType> {
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()


    override fun serialize(
        encoder: Encoder,
        value: OpdsPickType
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): OpdsPickType {
        return OpdsPickType.fromValue(decoder.decodeString())
    }
}