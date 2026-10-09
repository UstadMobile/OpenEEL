package world.respect.lib.xapi.remotewritequeue

import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import world.respect.lib.xapi.ext.decodeFromXapiDocument
import world.respect.lib.xapi.ext.isJson
import world.respect.lib.xapi.ext.toParametersFormUrlEncoded
import world.respect.lib.xapi.model.XapiDocument

/**
 * Represents a piece of Xapi data that needs written to the remote datasource.
 */
data class XapiRemoteWriteQueueItem(
    val xrqItemId: Int = 0,
    val method: Method,
    val resource: Resource,
    val itemId: String,
    val keysToPost: Set<String>? = null,
) {

    enum class Method(
        val flag : Int
    ) {

        POST(1),

        PUT(2),

        DELETE(3),

        ;

        companion object {

            fun fromFlag(flag: Int) = entries.first { it.flag == flag }

        }

    }

    enum class Resource(val flag: Int) {

        STATEMENTS(1),

        ACTIVITY_PROFILE(2),

        STATE(3),

        AGENT_PROFILE(4),

        ;

        companion object {

            fun fromFlag(flag: Int) = entries.first { it.flag == flag }

        }

    }


}