package org.openeel.datalayer.db.school.daos


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import org.openeel.datalayer.db.shared.entities.ReportQueryResultEntity

@Dao
interface ReportQueryResultDao {

    @Query("""
        DELETE FROM ReportQueryResultEntity
         WHERE rqrReportUid = :reportUid
           AND rqrTimeZone = :timeZone
    """)
    suspend fun deleteByReportUidAndTimeZone(reportUid: String, timeZone: String)

    @Insert
    suspend fun insertAllAsync(results: List<ReportQueryResultEntity>)


    @Query("""
        SELECT ReportQueryResultEntity.*
          FROM ReportQueryResultEntity
         WHERE ReportQueryResultEntity.rqrReportUid = :reportUid 
           AND ReportQueryResultEntity.rqrTimeZone = :timeZone
    """)
    suspend fun getAllByReportUidAndTimeZone(
        reportUid: String,
        timeZone: String
    ): List<ReportQueryResultEntity>

    /**
     * Determine if a previous report run is fresh (as the term is used in caching). This checks
     * that a) the results were generated after the report was last modified AND b) the result was
     * generated after the freshThresholdTime (eg does not exceed max age).
     *
     * @param reportUid reportUid
     * @param freshThresholdTime minimum timestamp for ReportQueryResultEntity to be considered fresh
     */
    @Query("""
        SELECT COALESCE(
               (SELECT ReportQueryResultEntity.rqrLastModified
                  FROM ReportQueryResultEntity
                 WHERE ReportQueryResultEntity.rqrReportUid = :reportUid
                   AND ReportQueryResultEntity.rqrTimeZone = :timeZone
                 LIMIT 1), 0) >= 
               (SELECT MAX(:freshThresholdTime, 
                            (SELECT COALESCE(
                                    (SELECT ReportEntity.rLastModified
                                       FROM ReportEntity
                                      WHERE ReportEntity.rGuid = :reportUid), 0))))
    """,)
    suspend fun isReportFresh(
        reportUid: String,
        timeZone: String,
        freshThresholdTime: Long,
    ): Boolean

    @RawQuery
    suspend fun executeRawQuery(query: RoomRawQuery): Long

    @Query("SELECT COUNT(*) FROM ReportQueryResultEntity WHERE rqrReportUid = :reportUid AND rqrTimeZone = :timeZone")
    suspend fun getResultCount(reportUid: String, timeZone: String): Int

}
