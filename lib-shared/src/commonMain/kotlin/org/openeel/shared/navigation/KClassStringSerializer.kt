package world.respect.shared.navigation

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

object KClassStringSerializer: KSerializer<KClass<*>> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.KClassStringSerializer", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: KClass<*>
    ) {
        encoder.encodeString(
            value.qualifiedName ?:
                throw IllegalArgumentException("KClassStringSerializer: KClass missing qualified name")
        )
    }

    override fun deserialize(decoder: Decoder): KClass<*> {
        return Class.forName(decoder.decodeString()).kotlin
    }
}

typealias KClassAsString = @Serializable(with = KClassStringSerializer::class) KClass<*>
