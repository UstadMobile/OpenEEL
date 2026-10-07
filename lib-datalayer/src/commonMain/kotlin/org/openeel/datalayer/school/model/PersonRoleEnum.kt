package org.openeel.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PersonRoleEnumSerializer::class)
enum class PersonRoleEnum(val value: String, val flag: Int) {
    SITE_ADMINISTRATOR("siteAdministrator", 1),
    STUDENT("student", 2),
    SYSTEM_ADMINISTRATOR("systemAdministrator", 3),
    TEACHER("teacher", 4),
    PARENT("parent", 5);

    companion object {

        const val SITE_ADMINISTRATOR_INT = 1

        const val STUDENT_INT = 2

        const val SYSTEM_ADMINISTRATOR_INT = 3

        const val TEACHER_INT = 4

        const val PARENT_INT = 5


        fun fromValue(value: String): PersonRoleEnum {
            return entries.first { it.value == value }
        }

        fun fromFlag(flag: Int): PersonRoleEnum {
            return entries.first { it.flag == flag }
        }

    }
}

object PersonRoleEnumSerializer: KSerializer<PersonRoleEnum> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.PersonRoleEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: PersonRoleEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): PersonRoleEnum {
        return PersonRoleEnum.fromValue(decoder.decodeString())
    }
}

