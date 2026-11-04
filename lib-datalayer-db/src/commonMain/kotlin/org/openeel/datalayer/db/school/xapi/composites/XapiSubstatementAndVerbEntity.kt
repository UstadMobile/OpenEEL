package org.openeel.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import org.openeel.datalayer.db.school.xapi.entities.XapiVerbEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStatementEntity

data class XapiSubstatementAndVerbEntity(
    @Embedded
    val stmtEntity: XapiStatementEntity,
    @Embedded
    val verbEntity: XapiVerbEntity,
)

