package org.openeel.lib.xapi.p2psync.model

import kotlinx.serialization.json.JsonObject

/**
 * Represents a peer node.
 *
 * @param nodeId Node id - set by the [org.openeel.lib.xapi.p2psync.XapiP2pSyncAdapter]
 * @param name a friendlier name that may be displayed to the user (where available)
 */
data class XapiP2pSyncNode(
    val nodeId: String,
    val name: String?,
    val metadata: JsonObject?,
)
