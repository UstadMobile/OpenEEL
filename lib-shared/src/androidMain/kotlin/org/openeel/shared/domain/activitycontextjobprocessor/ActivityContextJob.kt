package org.openeel.shared.domain.activitycontextjobprocessor

import android.app.Activity

fun interface ActivityContextJob {

    suspend fun doJob(activity: Activity)

}
