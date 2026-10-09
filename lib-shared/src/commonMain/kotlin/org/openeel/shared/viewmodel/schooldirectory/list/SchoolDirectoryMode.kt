package world.respect.shared.viewmodel.schooldirectory.list

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SchoolDirectoryModeSerializer::class)
enum class SchoolDirectoryMode(val value : String) {
    MANAGE("manage"),  // Shows edit/delete buttons
    SELECT("select");   // Shows clickable items for selection

    companion object {

        fun fromValue(value: String) = entries.first { it.value == value }

    }
}

object SchoolDirectoryModeSerializer: KSerializer<SchoolDirectoryMode> {
    override val descriptor = PrimitiveSerialDescriptor(
        "world.respect.SchoolDirectoryModeEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: SchoolDirectoryMode
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): SchoolDirectoryMode {
        return SchoolDirectoryMode.fromValue(decoder.decodeString())
    }

}
