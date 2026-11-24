package org.openeel.shared.domain.crawlpublicationsforuser

/**
 * Enqueue a crawl of publications for the active user (as per Koin-DI scope). On Android this uses
 * WorkManager to run [CrawlPublicationsForUserUseCase].
 */
interface EnqueueCrawlPublicationForUserUseCase {

    suspend operator fun invoke()

}