package world.respect.shared.domain.school

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

/**
 * Wrapper class used only for purposes of differentiating it for dependency injection purposes
 */
data class SchoolPrimaryKeyGenerator(
    val primaryKeyGenerator: PrimaryKeyGenerator = PrimaryKeyGenerator(TABLE_IDS)
) {
    companion object {

        val TABLE_IDS = RespectSchoolDatabase.TABLE_IDS

    }
}
