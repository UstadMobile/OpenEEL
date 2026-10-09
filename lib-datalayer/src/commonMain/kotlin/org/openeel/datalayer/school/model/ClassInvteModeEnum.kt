package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.respect.datalayer.school.model.Invite2.Companion.DIRECT_STR
import world.respect.datalayer.school.model.Invite2.Companion.VIA_PARENT_STR

@Serializable(with = ClassInviteModeEnumSerializer::class)
enum class ClassInviteModeEnum(val value: String, val flag: Int) {

    DIRECT(DIRECT_STR, 1), VIA_PARENT(VIA_PARENT_STR, 2);

    companion object {

        fun fromValue(value: String): ClassInviteModeEnum {
            return entries.first { it.value == value }
        }

        fun fromFlag(flag: Int): ClassInviteModeEnum {
            return entries.first { it.flag == flag }
        }

    }

}

object ClassInviteModeEnumSerializer: KSerializer<ClassInviteModeEnum> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.ClassInviteMode", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: ClassInviteModeEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): ClassInviteModeEnum {
        return ClassInviteModeEnum.fromValue(decoder.decodeString())
    }
}

