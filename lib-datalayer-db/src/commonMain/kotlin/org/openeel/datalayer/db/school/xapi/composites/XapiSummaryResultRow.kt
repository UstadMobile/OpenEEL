package org.openeel.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import org.openeel.datalayer.db.school.xapi.entities.XapiActorEntity


data class XapiSummaryResultRow(
    val activityUid: Long,
    val activityId: String,
    @Embedded
    val actorEntity: XapiActorEntity,
    val title: String?,
    val numCompleted: Int,
    val numTotal: Int,
    val deadlineStr: String?,
    val averageScoreScaled: Float? = null,
)
