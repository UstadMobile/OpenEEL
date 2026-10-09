package world.respect.lib.opds.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.respect.lib.serializers.StringOrObjectSerializer
import world.respect.lib.serializers.StringValue
import world.respect.lib.serializers.StringValueSerializer

/**
 * Schema https://readium.org/webpub-manifest/schema/language-map.schema.json
 *
 * Can be a simple string or a LangMap (language code to string map)
 */
@Serializable(with = LangMapSerializer::class)
sealed class LangMap {

    companion object {

        val EMPTY = LangMapStringValue("")

        fun fromMap(map: Map<String, String>) = LangMapObjectValue(map)

    }

}

object LangMapSerializer: StringOrObjectSerializer<LangMap>(
    baseClass = LangMap::class,
    primitiveSerializer = LangMapStringValue.serializer(),
    objectSerializer = LangMapObjectValue.serializer(),
)

@Serializable(with = LangMapStringValueSerializer::class)
data class LangMapStringValue(override val value: String): LangMap(), StringValue

object LangMapStringValueSerializer : StringValueSerializer<LangMapStringValue>(
    serialName = "LangMapStringValue", stringToValue = { LangMapStringValue(it) }
)

@Serializable(with = LangMapObjectValueSerializer::class)
data class LangMapObjectValue(val map: Map<String, String>): LangMap()

class LangMapObjectValueSerializer: KSerializer<LangMapObjectValue> {
    private val delegateSerializer = MapSerializer(String.serializer(), String.serializer())

    override val descriptor = SerialDescriptor(
        "world.respect.LangMapObjectValue", delegateSerializer.descriptor
    )

    override fun deserialize(decoder: Decoder) = LangMapObjectValue(
        delegateSerializer.deserialize(decoder)
    )

    override fun serialize(encoder: Encoder, value: LangMapObjectValue) {
        encoder.encodeSerializableValue(delegateSerializer, value.map)
    }
}
