package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = InviteStatusEnumSerializer::class)
enum class InviteStatusEnum(
    val value: String,
    val flag: Int
) {

    PENDING("pending", 0),

    ACCEPTED("accepted", 1),

    REVOKED("revoked", 2),

    TO_BE_DELETED("tobedeleted", -1);

    companion object {

        const val PENDING_INT = 0
        const val ACCEPTED_INT = 1
        const val REVOKED_INT = 2
        const val TO_BE_DELETED_INT = -1

        fun fromFlag(flag: Int): InviteStatusEnum =
            entries.first { it.flag == flag }

        fun fromValue(value: String): InviteStatusEnum =
            entries.first { it.value == value }
    }
}
object InviteStatusEnumSerializer : KSerializer<InviteStatusEnum> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "world.respect.InviteStatusEnum",
            PrimitiveKind.STRING
        )

    override fun serialize(
        encoder: Encoder,
        value: InviteStatusEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): InviteStatusEnum {
        return InviteStatusEnum.fromValue(
            decoder.decodeString()
        )
    }
}
