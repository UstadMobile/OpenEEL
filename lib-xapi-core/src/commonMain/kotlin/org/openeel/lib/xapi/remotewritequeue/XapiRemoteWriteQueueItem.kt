package org.openeel.lib.xapi.remotewritequeue

/**
 * Represents a piece of Xapi data that needs written to the remote datasource.
 *
 * @param xrqItemId the queue item id (normally auto-generated)
 * @param resource the resource to write to
 * @param method the HTTP method to use to write POST, PUT, or DELETE
 * @param itemId the ID of the item to send [org.openeel.lib.xapi.model.XapiStatement.id]
 *        for statements and the SingleDocumentParams query parameters URL-encoded for document
 *        resources.
 * @param keysToPost when method is POST, the json keys to post. As per the xAPI spec POST merges
 *        json documents by top level keys.
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