package world.respect.shared.viewmodel.manageuser.signup

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SignupScreenModeEnumSerializer::class)
enum class SignupScreenModeEnum(val value: String) {

    STANDARD("standard"), ADD_CHILD_TO_PARENT("addchild");

    companion object {

        fun fromValue(value: String): SignupScreenModeEnum {
            return SignupScreenModeEnum.entries.first { it.value == value }
        }
    }

}

object SignupScreenModeEnumSerializer: KSerializer<SignupScreenModeEnum> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.SignupScreenModeEnum", PrimitiveKind.STRING,
    )

    override fun serialize(
        encoder: Encoder,
        value: SignupScreenModeEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): SignupScreenModeEnum {
        return SignupScreenModeEnum.fromValue(decoder.decodeString())
    }
}
