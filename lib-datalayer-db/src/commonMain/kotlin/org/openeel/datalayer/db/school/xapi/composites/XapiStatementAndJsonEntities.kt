package org.openeel.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import org.openeel.datalayer.db.school.xapi.entities.XapiVerbEntity
import org.openeel.datalayer.db.school.xapi.entities.XapiStatementEntity

data class XapiStatementAndJsonEntities(
    @Embedded
    val stmtEntity: XapiStatementEntity,
    @Embedded
    val verbEntity: XapiVerbEntity?,
)