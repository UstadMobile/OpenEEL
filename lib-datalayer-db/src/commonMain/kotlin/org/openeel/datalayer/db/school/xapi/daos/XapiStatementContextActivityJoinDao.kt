package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiStatementContextActivityJoin

@Dao
interface XapiStatementContextActivityJoinDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<XapiStatementContextActivityJoin>)

    @Query(
        """
        SELECT XapiStatementContextActivityJoin.*
          FROM XapiStatementContextActivityJoin
         WHERE (      XapiStatementContextActivityJoin.scajFromStatementIdHi = :statementIdHi
                  AND XapiStatementContextActivityJoin.scajFromStatementIdLo = :statementIdLo)
            OR (     (:statementIdHi2 != 0 AND :statementIdLo2 != 0) 
                 AND XapiStatementContextActivityJoin.scajFromStatementIdHi = :statementIdHi2
                 AND XapiStatementContextActivityJoin.scajFromStatementIdLo = :statementIdLo2)
    """
    )
    suspend fun findAllByStatementIds(
        statementIdHi: Long,
        statementIdLo: Long,
        statementIdHi2: Long,
        statementIdLo2: Long,
    ): List<XapiStatementContextActivityJoin>
}