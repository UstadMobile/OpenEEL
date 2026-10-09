package org.openeel.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.http.Url
import org.openeel.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity
data class PersonQrBadgeEntity(
    @PrimaryKey
    val pqrGuidNum: Long,
    val pqrGuid: String,
    val pqrLastModified: Instant,
    val pqrStored: Instant,
    @ColumnInfo(index = true)
    val pqrQrCodeUrl: Url?,
    val pqrStatus: StatusEnum = StatusEnum.ACTIVE,
)
