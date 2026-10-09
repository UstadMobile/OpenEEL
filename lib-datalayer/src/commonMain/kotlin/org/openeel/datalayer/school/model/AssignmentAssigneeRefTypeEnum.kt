package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = AssignmentAssigneeRefTypeEnumSerializer::class)
enum class AssignmentAssigneeRefTypeEnum(val value: String, val flag: Int) {

    CLASS("class", 1);

    companion object {

        fun fromValue(value: String): AssignmentAssigneeRefTypeEnum {
            return entries.first { it.value == value }
        }

        fun fromFlag(flag: Int): AssignmentAssigneeRefTypeEnum {
            return entries.first { it.flag == flag }
        }

    }
}

object AssignmentAssigneeRefTypeEnumSerializer: KSerializer<AssignmentAssigneeRefTypeEnum> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.AssignmentAssigneeRefTypeEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: AssignmentAssigneeRefTypeEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): AssignmentAssigneeRefTypeEnum {
        return AssignmentAssigneeRefTypeEnum.fromValue(decoder.decodeString())
    }
}
