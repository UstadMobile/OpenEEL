package org.openeel.lib.xapi.p2psync

import io.ktor.utils.io.core.Closeable
import org.openeel.lib.xapi.resources.XapiResource

/**
 * [XapiP2pSyncAdapter] can be implemented to use different underlying communication channels e.g.
 * network service discovery, Google Nearby, Bluetooth, etc.
 */
interface XapiP2pSyncAdapter: Closeable {

    val adapterId: String

    val nodeTracker: NodeTracker

    suspend fun xapiResourceForNode(
        nodeId: String,
    ): XapiResource

}