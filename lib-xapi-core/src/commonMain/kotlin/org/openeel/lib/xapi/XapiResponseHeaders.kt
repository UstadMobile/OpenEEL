package org.openeel.lib.xapi

import kotlinx.serialization.Serializable
import org.openeel.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

@Serializable
data class XapiResponseHeaders(
    val lastModified: InstantAsISO8601 = Clock.System.now(),
) {
}