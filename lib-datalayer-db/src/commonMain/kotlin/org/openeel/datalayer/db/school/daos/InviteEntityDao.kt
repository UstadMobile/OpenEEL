package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.InviteEntity

@Dao
interface InviteEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inviteEntity: InviteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invites: List<InviteEntity>)

    @Query("""
        SELECT InviteEntity.iLastModified
          FROM InviteEntity
         WHERE InviteEntity.iGuidHash = :guidHash
         LIMIT 1
    """)
    suspend fun getLastModifiedByGuid(guidHash: Long): Long?

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iCode = :code
         LIMIT 1
    """)
    suspend fun getInviteByInviteCode(code:String): InviteEntity?
    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuidHash = :guidHash
         LIMIT 1
    """)
    suspend fun findByGuidHash(guidHash: Long): InviteEntity?

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuid = :guid
         LIMIT 1
    """)
    suspend fun findByGuid(guid: String): InviteEntity?

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuidHash = :guidHash
         LIMIT 1
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<InviteEntity?>

    @Query(LIST_SQL)

    fun listAsPagingSource(
        guidHash: Long = 0,
        code: String? = null,
    ): PagingSource<Int, InviteEntity>

    @Query(LIST_SQL)
    suspend fun list(
        guidHash: Long = 0,
        code: String? = null,
    ): List<InviteEntity>

    @Query("""
        SELECT * 
          FROM InviteEntity
         WHERE iGuidHash IN (:uidNums)
    """)
    suspend fun findByUidList(uidNums: List<Long>): List<InviteEntity>

    companion object {

        const val LIST_SQL = """
           SELECT InviteEntity.* 
             FROM InviteEntity
            WHERE (:guidHash = 0 OR InviteEntity.iGuidHash = :guidHash)
              AND (:code IS NULL OR InviteEntity.iCode = :code)
        """

    }
}
