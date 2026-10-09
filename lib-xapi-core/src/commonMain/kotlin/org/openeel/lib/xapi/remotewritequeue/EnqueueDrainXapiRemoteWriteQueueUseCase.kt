package world.respect.lib.xapi.remotewritequeue

/**
 * Enqueue a task to drain the remote write queue (which requires connectivity with the remote
 * datasource). On Android this is done using WorkManager. This follows the same patterns as
 * the main RemoteWriteQueue.
 */
fun interface EnqueueDrainXapiRemoteWriteQueueUseCase {

    suspend operator fun invoke()

}