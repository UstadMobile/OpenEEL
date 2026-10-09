package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.daos.PersonEntityDao.Companion.AUTHENTICATED_USER_PERSON_READ_PERMISSION_WHERE_CLAUSE_SQL
import world.respect.datalayer.db.school.entities.PersonPasswordEntity

@Dao
interface PersonPasswordEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(personPasswordEntity: PersonPasswordEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsyncList(list: List<PersonPasswordEntity>)

    @Query(
        """
           SELECT PersonPasswordEntity.* 
             FROM PersonPasswordEntity
            WHERE ppwGuidNum = :uidNum
        """
    )
    suspend fun findByUid(uidNum: Long): PersonPasswordEntity?

    @Query("""
        SELECT PersonPasswordEntity.ppwLastModified
          FROM PersonPasswordEntity
         WHERE PersonPasswordEntity.ppwGuidNum = :uidNum
    """)
    suspend fun getLastModifiedByPersonUidNum(
        uidNum: Long
    ): Long?

    @Query("""
        SELECT PersonPasswordEntity.*
          FROM PersonPasswordEntity
         WHERE PersonPasswordEntity.ppwGuidNum IN (:uids)
    """)
    suspend fun findByUidList(
        uids: List<Long>
    ) : List<PersonPasswordEntity>


    @Query(LIST_SQL)
    suspend fun findAll(
        authenticatedPersonUidNum: Long,
        personGuidNum: Long
    ): List<PersonPasswordEntity>

    @Query(LIST_SQL)
    fun findAllAsFlow(
        authenticatedPersonUidNum: Long,
        personGuidNum: Long
    ): Flow<List<PersonPasswordEntity>>



    companion object {

        const val LIST_SQL = """
          WITH ${PersonEntityDao.AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL},  
               ${PersonEntityDao.AUTHENTICATED_PERSON_CLASS_PERMISSIONS}
        
        SELECT PersonPasswordEntity.*
          FROM PersonPasswordEntity
               JOIN PersonEntity 
                    ON PersonEntity.pGuidHash = PersonPasswordEntity.ppwGuidNum
         WHERE PersonPasswordEntity.ppwGuidNum = :personGuidNum
           AND ($AUTHENTICATED_USER_PERSON_READ_PERMISSION_WHERE_CLAUSE_SQL)
        """

    }
}