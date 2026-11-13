package org.openeel.lib.xapi.remotewritequeue

import kotlinx.coroutines.flow.Flow

/**
 * Represents a queue of items that need to be sent to a remote datasource.
 */
interface XapiRemoteWriteQueue {

    suspend fun add(items: List<XapiRemoteWriteQueueItem>)

    suspend fun getPending(limit: Int): List<XapiRemoteWriteQueueItem>

    suspend fun markSent(ids: List<Int>)

    /**
     * Check if there is a pending write matching the given arguments
     *
     * @param resource The resource as per [XapiRemoteWriteQueueItem.resource]
     * @param itemId The queue itemId as per [XapiRemoteWriteQueueItem.itemId]
     * @param methods the HTTP method as per [XapiRemoteWriteQueueItem.method], or null
     */
    suspend fun isPending(
        resource: XapiRemoteWriteQueueItem.Resource,
        methods: List<XapiRemoteWriteQueueItem.Method>,
        itemId: String,
    ): Boolean

    /**
     * Get the size of the pending backlog as a flow. Useful to wait for the write queue to be
     * completely drained
     */
    fun queueSizeAsFlow(): Flow<Int>

}