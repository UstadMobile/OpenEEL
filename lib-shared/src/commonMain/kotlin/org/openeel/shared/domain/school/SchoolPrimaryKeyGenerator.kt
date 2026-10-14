package org.openeel.shared.domain.school

import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.lib.primarykeygen.PrimaryKeyGenerator

/**
 * Wrapper class used only for purposes of differentiating it for dependency injection purposes
 */
data class SchoolPrimaryKeyGenerator(
    val primaryKeyGenerator: PrimaryKeyGenerator = PrimaryKeyGenerator(TABLE_IDS)
) {
    companion object {

        val TABLE_IDS = SchoolDatabase.TABLE_IDS

    }
}
