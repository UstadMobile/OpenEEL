package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = StatusEnumSerializer::class)
enum class StatusEnum(val value: String, val flag: Int) {

    ACTIVE("active", 1), TO_BE_DELETED("tobedeleted", 0);



    companion object {

        const val ACTIVE_INT = 1

        @Suppress("unused") //Reserved for future use in DB queries which require const val
        const val TO_BE_DELETED_INT = 0


        fun fromFlag(flag: Int) = entries.first { it.flag == flag }

        fun fromValue(value: String) = entries.first { it.value == value }

    }
}

object StatusEnumSerializer: KSerializer<StatusEnum> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.StatusEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: StatusEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): StatusEnum {
        return StatusEnum.fromValue(decoder.decodeString())
    }

}
