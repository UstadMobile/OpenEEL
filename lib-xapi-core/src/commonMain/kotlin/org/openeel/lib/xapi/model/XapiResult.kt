package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import world.respect.lib.serializers.DurationAsISO8601

@Serializable
data class XapiResult(
    val completion: Boolean? = null,
    val success: Boolean? = null,
    val score: Score? = null,
    val duration: DurationAsISO8601? = null,
    val response: String? = null,
    val extensions: Map<String, JsonElement>? = null,
) {


    @Serializable
    data class Score(
        val scaled: Float? = null,
        val raw: Float? = null,
        val min: Float? = null,
        val max: Float? = null,
    )

}
