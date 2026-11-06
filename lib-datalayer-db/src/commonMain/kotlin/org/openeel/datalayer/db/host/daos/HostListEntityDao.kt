package org.openeel.datalayer.db.host.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.db.host.entities.HostListEntity

@Dao
interface HostListEntityDao {

    @Query("SELECT * FROM HostListEntity WHERE url = :url")
    fun findByUrlAsFlow(url: String): Flow<HostListEntity?>

    @Query("SELECT * FROM HostListEntity WHERE url = :url")
    suspend fun findByUrl(url: String): HostListEntity?

    @Upsert
    suspend fun upsert(entity: HostListEntity)
}
