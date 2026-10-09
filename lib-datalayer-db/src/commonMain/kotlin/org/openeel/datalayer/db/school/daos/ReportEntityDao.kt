package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.ReportEntity

@Dao
interface  ReportEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putReport(reportEntity: ReportEntity)

    @Query(
        """
        SELECT * 
        FROM ReportEntity
        WHERE rIsTemplate = :template
    """
    )
    fun getAllReportsByTemplate(template: Boolean): Flow<List<ReportEntity>>

    @Query(
        """
        SELECT * 
         FROM ReportEntity
        WHERE rGuid = :reportId
    """
    )
    suspend fun getReportAsync(reportId: String): ReportEntity?

    @Query(
        """
        SELECT * 
         FROM ReportEntity
        WHERE rGuid = :reportId
    """
    )
    fun getReportAsFlow(reportId: String): Flow<ReportEntity?>

    @Query("DELETE FROM ReportEntity WHERE rGuid = :reportUid")
    suspend fun deleteReportByUid(reportUid: String)
}