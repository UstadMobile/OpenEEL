package world.respect.datalayer.school.writequeue

/**
 * Enqueue a task to drain the remote write queue (which requires connectivity with the remote
 * datasource). On Android this is done using WorkManager.
 */
fun interface EnqueueDrainRemoteWriteQueueUseCase {

    suspend operator fun invoke()

}