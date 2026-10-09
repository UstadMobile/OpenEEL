package world.respect.lib.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Sometimes JSON schema polymorphism allows a String or Object to be used as a value. This simple
 * interface can be applied to sealed classes (e.g. ContributorStringValue), which can then shorten
 * the serializer boilerplate code required.
 */
interface StringValue {

    val value: String

}

abstract class StringValueSerializer<T: StringValue>(
    serialName: String,
    private val stringToValue: (String) -> T
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName, PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.value)

    override fun deserialize(decoder: Decoder): T {
        return stringToValue(decoder.decodeString())
    }
}
