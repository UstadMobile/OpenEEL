package org.openeel.lib.xapi.p2psync.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class XapiP2pSyncNodeEntity(
    @PrimaryKey
    val nodeId: String,
    val nodeName: String,
    val nodeAvailable: Boolean,
)
