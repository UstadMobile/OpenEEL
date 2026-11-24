package org.openeel.lib.xapi.p2psync.poll

import org.openeel.lib.xapi.p2psync.NodeTracker
import org.openeel.lib.xapi.resources.local.XapiResourceLocal

/**
 * Check all available peers for any new statements and pull them into the local database. This
 * represents a "belt and suspenders" approach : normally, all statements should be pushed between
 * nodes.
 */
class PollPeersUseCase(
    private val nodeTracker: NodeTracker,
    private val local: XapiResourceLocal,
) {

    suspend fun invoke() {

    }

}