package world.respect.lib.serializers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * Some schemas (e.g. https://readium.org/webpub-manifest/schema/metadata.schema.json) allow fields
 * that can be a LocalDate YYYY-MM-DD or LocalDateTime YYYY-MM-DDThh:mm:ssZ.
 *
 * This is handled using a sealed class; when deserializing the length of the string is checked to
 * decide which serializer to use.
 */
@Serializable(with = LocalDateOrDateTimeSerializer::class)
sealed class LocalDateOrDateTime

@Serializable(with = LocalDateValueSerializer::class)
class LocalDateValue(val value: LocalDate): LocalDateOrDateTime() {

}

object LocalDateValueSerializer: KSerializer<LocalDateValue> {
    private val delegateSerializer = LocalDateIso8601Serializer

    override val descriptor = SerialDescriptor(
        "world.respect.LocalDateValue", delegateSerializer.descriptor,
    )

    override fun deserialize(decoder: Decoder) = LocalDateValue(
        decoder.decodeSerializableValue(delegateSerializer)
    )

    override fun serialize(encoder: Encoder, value: LocalDateValue) {
        encoder.encodeSerializableValue(delegateSerializer, value.value)
    }
}

@Serializable(with = LocalDateTimeValueSerializer::class)
class LocalDateTimeValue(val value: LocalDateTime): LocalDateOrDateTime()

object LocalDateTimeValueSerializer: KSerializer<LocalDateTimeValue> {
    private val delegateSerializer = LocalDateTimeIso8601Serializer

    override val descriptor = SerialDescriptor(
        "world.respect.LocalDateTimeValue", delegateSerializer.descriptor
    )

    override fun deserialize(decoder: Decoder) = LocalDateTimeValue(
        decoder.decodeSerializableValue(delegateSerializer)
    )

    override fun serialize(encoder: Encoder, value: LocalDateTimeValue) {
        encoder.encodeSerializableValue(delegateSerializer, value.value)
    }
}

object LocalDateOrDateTimeSerializer: JsonContentPolymorphicSerializer<LocalDateOrDateTime>(
    LocalDateOrDateTime::class
) {

    private const val LOCAL_DATE_ONLY_LENGTH = 10

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<LocalDateOrDateTime> {
        val jsonPrimitive = element.jsonPrimitive
        if(!jsonPrimitive.isString)
            throw IllegalArgumentException("LocalDateOrDateTime: Expected string value")

        val jsonContentStr = jsonPrimitive.content

        return if(jsonContentStr.length == LOCAL_DATE_ONLY_LENGTH) {
            LocalDateValue.serializer()
        }else {
            LocalDateTimeValue.serializer()
        }
    }

}

