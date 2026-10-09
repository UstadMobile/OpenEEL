package world.respect.datalayer.school.writequeue

import kotlinx.coroutines.flow.Flow

/**
 * Represents a queue of items to be sent to the remote datasource. This queue is used by the
 * Repository. When write operations are run on the repository it will update the local datasource
 * and then add a WriteQueueItem for the items changed.
 *
 * The write queue is drained by a job (e.g. running through WorkManager on Android) that will send
 * write items to the network datasource when a connection is available and resolves any conflicts.
 *
 * The RemoteWriteQueue, like the SchoolDataSource, is tied to a specific authenticated user. There
 * is no RemoteWriteQueue for SchoolDirectoryDataSource or other app level data sources (e.g. OPDS,
 * App Manifests, etc) because that data is read-only for the RESPECT app.
 */
interface RemoteWriteQueue {

    suspend fun add(items: List<WriteQueueItem>)

    suspend fun getPending(limit: Int): List<WriteQueueItem>

    suspend fun markSent(ids: List<Int>)

    /**
     * Get the size of the pending backlog as a flow. Useful to wait for the write queue to be
     * completely drained
     */
    fun queueSizeAsFlow(): Flow<Int>


}