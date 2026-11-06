package org.openeel.datalayer.db.host.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A complete catalog snapshot, including empty catalogs, isolated by its source URL. */
@Entity
data class HostListEntity(
    @PrimaryKey
    val url: String,
    val hostsJson: String,
    val etag: String?,
    val lastModified: String?,
)
