package world.respect.lib.xapi.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object XapiInteractionTypeFlags {
    
    const val TYPE_UNSET = 0

    const val TYPE_TRUE_FALSE = 1

    const val TYPE_CHOICE = 2

    const val TYPE_FILL_IN = 3

    const val TYPE_LONG_FILL_IN = 4

    const val TYPE_MATCHING = 5

    const val TYPE_PERFORMANCE = 6

    const val TYPE_SEQUENCING = 7

    const val TYPE_LIKERT = 8

    const val TYPE_NUMERIC = 9

    const val TYPE_OTHER = 10


}

/**
 * Valid InteractionType properties as per:
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 */
@Serializable(with = XapiInteractionTypeSerializer::class)
enum class XapiInteractionTypeEnum(
    val jsonFieldValue: String,
    val dbFlag: Int,
) {

    TrueFalse("true-false", XapiInteractionTypeFlags.TYPE_TRUE_FALSE),
    Choice("choice", XapiInteractionTypeFlags.TYPE_CHOICE),
    FillIn("fill-in", XapiInteractionTypeFlags.TYPE_FILL_IN),
    LongFillIn("long-fill-in", XapiInteractionTypeFlags.TYPE_LONG_FILL_IN),
    Matching("matching", XapiInteractionTypeFlags.TYPE_MATCHING),
    Performance("performance", XapiInteractionTypeFlags.TYPE_PERFORMANCE),
    Sequencing("sequencing", XapiInteractionTypeFlags.TYPE_SEQUENCING),
    Likert("likert", XapiInteractionTypeFlags.TYPE_LIKERT),
    Numeric("numeric", XapiInteractionTypeFlags.TYPE_NUMERIC),
    Other("other", XapiInteractionTypeFlags.TYPE_OTHER);

    companion object {


        fun fromJsonFieldValue(value: String): XapiInteractionTypeEnum {
            return entries.firstOrNull { it.jsonFieldValue == value } ?: Other
        }

        @Suppress("unused")
        fun fromDbFlag(value: Int) : XapiInteractionTypeEnum? {
            return entries.firstOrNull { it.dbFlag == value }
        }
    }


}

object XapiInteractionTypeSerializer: KSerializer<XapiInteractionTypeEnum> {
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()

    override fun deserialize(decoder: Decoder): XapiInteractionTypeEnum {
        return XapiInteractionTypeEnum.fromJsonFieldValue(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: XapiInteractionTypeEnum) {
        encoder.encodeString(value.jsonFieldValue)
    }

}
