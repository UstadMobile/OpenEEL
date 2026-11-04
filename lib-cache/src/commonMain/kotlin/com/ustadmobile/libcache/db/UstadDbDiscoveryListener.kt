package com.ustadmobile.libcache.db

import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.distributed.DistributedCacheNeighborDiscoveryListener
import com.ustadmobile.libcache.distributed.neighborUid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.openeel.libxxhash.XXStringHasher
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UstadDbDiscoveryListener(
    private val db: UstadCacheDb,
    private val scope: CoroutineScope,
    private val xxStringHasher: XXStringHasher,
): DistributedCacheNeighborDiscoveryListener {

    @OptIn(ExperimentalTime::class)
    override fun onNeighborDiscovered(neighborIp: String, neighborUdpPort: Int, neighborHttpPort: Int) {
        scope.launch {
            db.neighborCacheDao.upsertAsync(
                NeighborCache(
                    neighborUid = xxStringHasher.neighborUid(neighborIp, neighborUdpPort),
                    neighborIp = neighborIp,
                    neighborDiscovered = Clock.System.now().toEpochMilliseconds(),
                    neighborPingTime = 0,
                    neighborHttpPort = neighborHttpPort,
                    neighborUdpPort = neighborUdpPort,
                )
            )
        }
    }

    override fun onNeighborLost(neighborIp: String, neighborUdpPort: Int) {
        scope.launch {
            db.neighborCacheDao.deleteAsync(
                neighborUid = xxStringHasher.neighborUid(neighborIp, neighborUdpPort),
            )
        }
    }

    fun close() {
        scope.cancel()
    }
}