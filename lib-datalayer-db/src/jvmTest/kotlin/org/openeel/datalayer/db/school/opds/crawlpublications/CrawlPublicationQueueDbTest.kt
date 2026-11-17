package org.openeel.datalayer.db.school.opds.crawlpublications

import app.cash.turbine.test
import io.ktor.http.Url
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.datalayer.db.school.testSchoolDb
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItem
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItemStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class CrawlPublicationQueueDbTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun givenExistingJob_whenNewJobIsCreated_thenNextJobIdIsAllocated() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val queue = CrawlPublicationQueueDbImpl(db)
            queue.insertItemsIfNotInJob(
                items = listOf(
                    CrawlPublicationQueueItem(
                        jobId = 12, itemUrl = Url("https://example.org/existing")
                    )
                ),
                newJob = false,
            )

            val newJobId = queue.insertItemsIfNotInJob(
                items = listOf(
                    CrawlPublicationQueueItem(
                        jobId = 1, itemUrl = Url("https://example.org/new")
                    )
                ),
                newJob = true,
            )

            assertEquals(13, newJobId)
            val newJobItem = queue.queuedItemsFlow(newJobId).first().single()
            assertEquals(newJobId, newJobItem.jobId)
            assertEquals(Url("https://example.org/new"), newJobItem.itemUrl)
        }
    }

    @Test
    fun givenSameUrlInQueue_whenStatusChanges_thenFlowsReflectQueuedAndRemainingItems() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val queue = CrawlPublicationQueueDbImpl(db)
            val initialNumItems = 10
            val extraNumItems = 6
            val jobId = 1

            queue.insertItemsIfNotInJob(
                items = (0 until initialNumItems).map {
                    CrawlPublicationQueueItem(
                        jobId = jobId, itemUrl = Url("https://example.org/shared-$it"),
                    )
                },
                newJob = false,
            )


            //should not be added - URL is already in the job
            queue.insertItemsIfNotInJob(
                items = listOf(
                    CrawlPublicationQueueItem(
                        jobId = jobId, itemUrl = Url("https://example.org/shared-0"),
                    )
                ),
                newJob = false,
            )

            queue.queuedItemsFlow(jobId).test(timeout = 5.seconds) {
                val initialQueue = awaitItem()
                assertEquals(initialNumItems, queue.remainingItemsCount(jobId).first())
                assertEquals(initialNumItems, initialQueue.size)

                //Set status on half of items to be running
                val firstHalf = initialQueue.filterIndexed { index, _ -> index.mod(2) == 0 }
                val secondHalf = initialQueue.filter { it !in firstHalf }
                queue.updateStatus(
                    firstHalf.map { it.itemId }, CrawlPublicationQueueItemStatus.RUNNING
                )

                val queueAfterStatusUpdate = awaitItemWhere { it.size == initialNumItems / 2 }
                assertEquals(initialNumItems / 2, queueAfterStatusUpdate.size)
                assertEquals(initialNumItems, queue.remainingItemsCount(jobId).first())

                //Update those items as done
                queue.updateStatus(firstHalf.map { it.itemId }, CrawlPublicationQueueItemStatus.DONE)
                queue.remainingItemsCount(jobId).first { it == initialNumItems / 2 }

                queue.updateStatus(secondHalf.map { it.itemId }, CrawlPublicationQueueItemStatus.RUNNING)
                val queueAfterFirstHalfRunning = awaitItemWhere { it.isEmpty() }
                assertTrue(queueAfterFirstHalfRunning.isEmpty())
                assertEquals(initialNumItems / 2, queue.remainingItemsCount(jobId).first())

                //Simulate finding extra items
                queue.insertItemsIfNotInJob(
                    items = (0 until extraNumItems).map {
                        CrawlPublicationQueueItem(
                            jobId = jobId, itemUrl = Url("https://example.org/shared-extra-$it"),
                        )
                    },
                    newJob = false,
                )

                queue.updateStatus(secondHalf.map { it.itemId }, CrawlPublicationQueueItemStatus.DONE)

                assertEquals(
                    extraNumItems,
                    queue.remainingItemsCount(jobId).first {
                        it == extraNumItems
                    }
                )
                val extraItems = awaitItemWhere { queueItems ->
                    queueItems.size == extraNumItems && queueItems.all {
                        it.itemUrl.toString().contains("extra")
                    }
                }

                queue.updateStatus(
                    extraItems.map { it.itemId }, CrawlPublicationQueueItemStatus.RUNNING
                )
                awaitItemWhere { it.isEmpty() }
                queue.updateStatus(
                    extraItems.map { it.itemId }, CrawlPublicationQueueItemStatus.DONE
                )

                assertEquals(0, queue.remainingItemsCount(jobId).first { it == 0 })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}