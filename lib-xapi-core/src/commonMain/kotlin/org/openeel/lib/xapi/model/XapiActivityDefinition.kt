package world.respect.lib.xapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Activity definition as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#activity-definition
 *
 */
@Serializable
@SerialName("Activity")
data class XapiActivityDefinition(
    val name: Map<String, String>? = null,
    val description: Map<String, String>? = null,
    val type: String? = null,
    val extensions: Map<String, JsonElement>? = null,
    val moreInfo: String? = null,
    val interactionType: XapiInteractionTypeEnum? = null,
    val correctResponsesPattern: List<String>? = null,
    val choices: List<Interaction>? = null,
    val scale: List<Interaction>? = null,
    val source: List<Interaction>? = null,
    val target: List<Interaction>? = null,
    val steps: List<Interaction>? = null,
) {

    @Serializable
    data class Interaction(
        val id: String,
        val description: Map<String, String>? = null
    )

    companion object {

        const val TYPE_ASSIGNMENT = "http://id.tincanapi.com/activitytype/school-assignment"

    }
}
