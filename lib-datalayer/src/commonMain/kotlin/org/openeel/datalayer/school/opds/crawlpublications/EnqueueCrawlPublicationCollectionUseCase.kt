package org.openeel.datalayer.school.opds.crawlpublications

import io.ktor.http.Url

/**
 * Enqueue a publication crawl. On Android this uses WorkManager to enqueue the crawl (using the
 * required network connected constraint to ensure that it runs only when connectivity is available.
 *
 * Running the publication crawl ensures that the
 */
interface EnqueueCrawlPublicationCollectionUseCase {

    data class Request(
        val url: Url,
    )

    suspend operator fun invoke(
        request: Request
    )

}