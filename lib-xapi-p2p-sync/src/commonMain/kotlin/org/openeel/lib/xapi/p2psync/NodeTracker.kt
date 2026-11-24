package org.openeel.lib.xapi.p2psync

import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.xapi.p2psync.model.XapiP2pSyncNode
import org.openeel.lib.xapi.resources.XapiResource

/**
 * The [NodeTracker] is responsible to track available peers and provide an [XapiResource] for a
 * given node on demand.
 */
interface NodeTracker: Closeable {

    /**
     * The flow should be hot.
     */
    fun availablePeers(): Flow<List<XapiP2pSyncNode>>

}