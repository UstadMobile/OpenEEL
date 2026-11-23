package org.openeel.shared.domain.crawlpublicationsforuser

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.datalayer.school.opds.crawlpublications.EnqueueCrawlPublicationCollectionUseCase
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.findCollection
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.ext.objectIdOrNull
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.libutil.ext.resolve

/**
 * Crawl feeds for the user (as per the Koin-DI scope) so that subsequent catalog browsing is fast
 * and works offline
 *
 * This should be enqueued on login or enabling school shared device mode.
 * @param xapiResource [XapiResource] for the scoped user
 * @param enqueueCrawlPublicationCollectionUseCase [EnqueueCrawlPublicationForUserUseCase] use to
 *        run a crawl of each publication.
 */
class CrawlPublicationsForUserUseCase(
    private val xapiResource: XapiResource,
    private val opdsPublicationDataSource: OpdsPublicationDataSource,
    private val enqueueCrawlPublicationCollectionUseCase: EnqueueCrawlPublicationCollectionUseCase,
) {

    suspend operator fun invoke() {
        val urlsToCrawl = xapiResource.statements.get(
            listParams = XapiStatementsResource.GetStatementParams(
                verb = XapiVerb.ID_LISTED_APP,
                activity = OpenEelXapiConstants.CATEGORY_APP_LISTING_RECIPE,
                relatedActivities = true,
            ),
            dataLoadParams = DataLoadParams(),
        ).dataOrNull()?.statements?.mapNotNull { it.objectIdOrNull() }.orEmpty().distinct()

        urlsToCrawl.forEach { urlStr ->
            val pubUrl = Url(urlStr)
            val appPublication = opdsPublicationDataSource.getByUrl(
                url = pubUrl,
                params = DataLoadParams(),
            ).dataOrNull()

            if(appPublication != null) {
                appPublication.findCollection()?.also {
                    enqueueCrawlPublicationCollectionUseCase(
                        EnqueueCrawlPublicationCollectionUseCase.Request(
                            url = pubUrl.resolve(it.href),
                        )
                    )
                }
            }else {
                Napier.w(tag = LOGTAG, message = "could not load $pubUrl")
            }
        }
    }

    companion object {

        const val LOGTAG = "CrawlPublicationForUserUseCase"
    }

}