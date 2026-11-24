package org.openeel.lib.xapi.p2psync.push

import org.openeel.lib.xapi.p2psync.NodeTracker
import org.openeel.lib.xapi.p2psync.XapiP2pSyncAdapter
import org.openeel.lib.xapi.resources.local.XapiResourceLocal

/**
 * Push any statements made locally since the last run to all available peers. We need to know
 * which ones actually originated here.
 *
 * @param nodeTracker
 */
class PushStatementsToPeersUseCase(
    private val local: XapiResourceLocal,
    private val nodeTracker: NodeTracker,
    private val p2pSyncAdapter: XapiP2pSyncAdapter
) {

    /**
     * For each node that is available:
     *   a) Run a query to find those EnqueuedStatementEntity(s) that were stored before the
     *      pushConsistentThrough time for the node.
     *   b) Post the statements to the node
     *   c) Update the pushedConsistentThrough time for the node
     */
    suspend operator fun invoke() {

    }

}