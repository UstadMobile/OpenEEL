package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class XapiAttachment(
    val usageType: String? = null,

    val display: Map<String, String>? = null,

    val description: Map<String, String>? = null,

    val contentType: String? = null,

    val length: Long = 0,

    val sha2: String? = null,
)
