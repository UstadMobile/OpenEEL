package world.respect.lib.serializers

import com.eygraber.uri.Uri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object UriStringSerializer: KSerializer<Uri> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.UriStringSerializer", PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder)= Uri.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }

}