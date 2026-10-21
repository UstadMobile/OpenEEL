package org.openeel.datalayer.db.school.xapi.writequeue

import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.db.school.testSchoolDb
import org.openeel.lib.xapi.remotewritequeue.EnqueueDrainXapiRemoteWriteQueueUseCase
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XapiRemoteWriteQueueDbTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun givenItemQueued_whenIsPendingCalled_thenResultMatches() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val account = AuthenticatedUserPrincipalId("user-1")
            val noopEnqueue: EnqueueDrainXapiRemoteWriteQueueUseCase = {}

            val queue = XapiRemoteWriteQueueDbImpl(
                schoolDb = db,
                account = account,
                enqueueDrainRemoteWriteQueueUseCase = noopEnqueue,
            )

            suspend fun isPending() = queue.isPending(
                resource = XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE,
                itemId = "doc-1",
            )

            //Should be false when empty
            assertFalse(isPending())

            queue.add(
                listOf(
                    XapiRemoteWriteQueueItem(
                        method = XapiRemoteWriteQueueItem.Method.POST,
                        resource = XapiRemoteWriteQueueItem.Resource.ACTIVITY_PROFILE,
                        itemId = "doc-1",
                    )
                )
            )

            //True when item is pending
            assertTrue(isPending())

            val pendingItems = queue.getPending(10)
            queue.markSent(pendingItems.map { it.xrqItemId })

            //False again after being sent
            assertFalse(isPending())
        }
    }

}
