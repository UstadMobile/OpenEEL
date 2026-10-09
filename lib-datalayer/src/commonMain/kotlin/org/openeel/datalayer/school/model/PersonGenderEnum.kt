package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PersonGenderEnumSerializer::class)
enum class PersonGenderEnum(val value : String, val flag : Int) {

    FEMALE("female", 1),
    MALE("male", 2),
    OTHER("other", 3),
    UNSPECIFIED("unspecified", 4);

    companion object {

        fun fromValue(value: String): PersonGenderEnum {
            return entries.first { it.value == value }
        }

        fun fromFlag(flag: Int): PersonGenderEnum {
            return entries.first { it.flag == flag }
        }

    }

}

object PersonGenderEnumSerializer: KSerializer<PersonGenderEnum> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.PersonGenderEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: PersonGenderEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): PersonGenderEnum {
        return PersonGenderEnum.fromValue(decoder.decodeString())
    }

}
