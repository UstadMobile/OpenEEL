package org.openeel.lib.xapi.p2psync.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
class XapiP2pSyncEnqueuedStatementEntity(
    @PrimaryKey
    val uuid: String,
    val stored: Instant = Clock.System.now(),
)
