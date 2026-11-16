package org.openeel.datalayer.school.opds.crawlpublications

import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Crawls a [org.openeel.lib.opds.model.Publication]'s collections.
 */
class CrawlPublicationCollectionsUseCase {

    val scope = CoroutineScope(Dispatchers.Default + Job())

    data class JobItem(
        val url: Url,
        val status: Int = QUEUED,
    )

    val allJobs = MutableStateFlow(listOf<JobItem>())


    //Could search db based on job id or such . Produce itself is asynchronous
    fun CoroutineScope.produceJobs() = produce<JobItem> {
        //collect a flow from the database that has all pending jobs.
        allJobs.collect { jobs ->
            val jobsToStart = jobs.filter { it.status == QUEUED }
            println("Jobs to start: ${jobsToStart.joinToString()} ")

            if(jobsToStart.isNotEmpty()) {
                allJobs.update {
                    jobs.map {
                        it.copy(
                            status = if(jobsToStart.contains(it)) {
                                RUNNING
                            }else {
                                it.status
                            }
                        )
                    }
                }

                jobsToStart.forEach { send(it.copy(status = RUNNING)) }
            }
        }

    }

    fun CoroutineScope.launchProcessor(
        id: Int,
        channel: ReceiveChannel<JobItem>,
        depthNum: Int = 2,
    ) = launch {
        var numItemsReceived = 0
        for(item in channel) {
            delay(100.milliseconds)
            println("Processor $id processed job: $item")

            allJobs.update { jobList ->
                jobList.map {
                    if(it == item) {
                        it.copy(status = DONE)
                    }else {
                        it
                    }
                } + if(numItemsReceived < depthNum) {
                    (0..10).map { index ->
                        JobItem(Url(item.url.toString() + "_$id-$numItemsReceived-$index"))
                    }
                }else {
                    emptyList()
                }
            }

            println(" Processor $id update job list")

            numItemsReceived++
        }

        println("Processor $id done")
    }

    suspend operator fun invoke(
        url: Url
    ) {
        allJobs.update { listOf(JobItem(url)) }

        coroutineScope {
            val producer = produceJobs()
            repeat(NUM_PROCESSORS) {
                println("Launch processor $it update job list")
                launchProcessor(it, producer)
            }

            println("Wait for all jobs to be done")
            allJobs.map { jobs ->
                jobs.count { it.status != DONE }
            }.first { it == 0 }
            println("All done - cancelling")
            producer.cancel()
        }

        println("All done")

    }

    companion object {

        const val QUEUED = 0
        const val RUNNING = 1
        const val DONE = 2

        const val NUM_PROCESSORS = 10

    }

}