package org.openeel.lib.xapi.p2psync.db

import org.openeel.lib.xapi.p2psync.model.XapiP2pSyncNode

interface XapiP2pSyncDataSource {

    suspend fun addNode(xapiP2pSyncNode: XapiP2pSyncNode)

    suspend fun setNodeAvailable(
        nodeId: String,
        available: Boolean
    )

}