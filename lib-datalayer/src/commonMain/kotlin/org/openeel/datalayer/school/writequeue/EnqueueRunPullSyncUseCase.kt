package world.respect.datalayer.school.writequeue

fun interface EnqueueRunPullSyncUseCase {

    suspend operator fun invoke()

}