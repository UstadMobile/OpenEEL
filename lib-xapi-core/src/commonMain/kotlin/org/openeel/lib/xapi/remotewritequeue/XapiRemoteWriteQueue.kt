package world.respect.lib.xapi.remotewritequeue

import kotlinx.coroutines.flow.Flow

/**
 * Represents a queue of items that need to be sent to a remote datasource.
 */
interface XapiRemoteWriteQueue {

    suspend fun add(items: List<XapiRemoteWriteQueueItem>)

    suspend fun getPending(limit: Int): List<XapiRemoteWriteQueueItem>

    suspend fun markSent(ids: List<Int>)

    /**
     * Get the size of the pending backlog as a flow. Useful to wait for the write queue to be
     * completely drained
     */
    fun queueSizeAsFlow(): Flow<Int>

}