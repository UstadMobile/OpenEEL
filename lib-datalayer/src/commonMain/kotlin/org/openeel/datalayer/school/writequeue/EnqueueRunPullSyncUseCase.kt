package org.openeel.datalayer.school.writequeue

fun interface EnqueueRunPullSyncUseCase {

    suspend operator fun invoke()

}