package world.respect.lib.xapi

import kotlinx.serialization.Serializable
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

@Serializable
data class XapiResponseHeaders(
    val lastModified: InstantAsISO8601 = Clock.System.now(),
) {
}