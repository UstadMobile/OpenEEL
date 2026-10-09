package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PersonStatusEnumSerializer::class)
enum class PersonStatusEnum(val value: String, val flag: Int) {

    ACTIVE("active", 1),

    TO_BE_DELETED("tobedeleted", 0),

    /**
     * Indicates that the person has requested to join using an invite code and approval is pending.
     */
    PENDING_APPROVAL("pendingapproval", -1);

    companion object {

        @Suppress("unused") //Reserved
        const val ACTIVE_INT = 1

        const val TO_BE_DELETED_INT = 0

        @Suppress("unused") //Reserved
        const val PENDING_APPROVAL_INT = -1

        fun fromFlag(flag: Int): PersonStatusEnum {
            return entries.first { it.flag == flag }
        }

        fun fromValue(value: String): PersonStatusEnum {
            return entries.first { it.value == value }
        }

    }
}


object PersonStatusEnumSerializer: KSerializer<PersonStatusEnum> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.PersonStatusEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: PersonStatusEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): PersonStatusEnum {
        return PersonStatusEnum.fromValue(decoder.decodeString())
    }

}