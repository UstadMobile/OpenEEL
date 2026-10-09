package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.Uuid

@Serializable
data class XapiContext(
    val instructor: XapiActor? = null,

    val registration: Uuid? = null,

    val language: String? = null,

    val platform: String? = null,

    val revision: String? = null,

    val team: XapiActor? = null,

    val statement: XapiActivity? = null,

    val contextActivities: XapiContextActivities? = null,

    val extensions: Map<String, JsonElement>? = null,
)
