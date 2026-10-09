package world.respect.lib.xapi.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = XapiObjectTypeSerializer::class)
enum class XapiObjectType(val value: String) {
    StatementRef("StatementRef"),
    SubStatement("SubStatement"),
    Activity("Activity"),
    Agent("Agent"),
    Group("Group"),
    Statement("Statement");

    companion object {

        fun fromString(value: String): XapiObjectType {
            return entries.first { it.value == value }
        }

    }

}


object XapiObjectTypeSerializer: KSerializer<XapiObjectType> {

    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()

    override fun deserialize(decoder: Decoder): XapiObjectType {
        return XapiObjectType.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: XapiObjectType) {
        encoder.encodeString(value.value)
    }

}
