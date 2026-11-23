package org.openeel.shared.domain.opds.getxapiactivityid

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.adaptivity.xmlutil.serialization.XML
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ext.allPublications
import org.openeel.lib.opds.model.findLearningUnitAcquisitionLinks
import org.openeel.lib.opds.model.findSelfLinks
import org.openeel.lib.opds.model.findTinCanXmlLink
import org.openeel.lib.opds.model.toStringMap
import org.openeel.lib.xapi.OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiActivityDefinition
import org.openeel.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import org.openeel.libutil.ext.resolve
import org.openeel.libutil.ext.resolveSafely
import org.openeel.shared.util.ext.legacyActivityIdForLink
import org.openeel.shared.viewmodel.catalog.PublicationsSelection

/**
 * Get an [XapiActivity] (including definition) for a given publication. If the publication includes
 * a link to a tincan.xml file, then [XapiActivity.id], [XapiActivityDefinition.name], and
 * [XapiActivityDefinition.description] will be set from the tincan.xml file.
 *
 * Otherwise, the publication's identifier will be used as the id. See README_XAPI_OPDS.md
 */
class GetXapiActivityForPublicationUseCase(
    private val xml: XML,
    private val httpClient: HttpClient,
) {

    suspend operator fun invoke(
        publication: Publication,
        publicationUrl: Url,
    ) : XapiActivity {
        val tinCanXmlLink = publication.findTinCanXmlLink()
        return if(tinCanXmlLink != null) {
            val tinCanXmlUrl = publicationUrl.resolve(tinCanXmlLink.href)
            val tinCanXmlDocument = httpClient.get(tinCanXmlUrl).bodyAsText().let {
                xml.decodeFromString(TinCanXmlDocument.serializer(), it)
            }

            val tinCanXmlActivity = tinCanXmlDocument.activities.activity.firstOrNull()
                ?: throw IllegalArgumentException("GetXapiActivityForPublicationUseCase: no activity element found in $tinCanXmlUrl")

            val description = tinCanXmlActivity.description
            val activityType = tinCanXmlActivity.type
            XapiActivity(
                id = tinCanXmlActivity.id,
                definition = XapiActivityDefinition(
                    type = activityType,
                    name = publication.metadata.title.toStringMap(noLangKey = "en-US"),
                    description = description?.let { mapOf(it.lang to it.value) },
                    extensions = JsonObject(
                        mapOf(ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK to JsonPrimitive(publicationUrl.toString()))
                    )
                )
            )
        }else {
            val activityId = publication.findLearningUnitAcquisitionLinks().firstOrNull()?.let {
                publication.legacyActivityIdForLink(it, publicationUrl)
            } ?: throw IllegalArgumentException("Cannot determine xAPI activityId for publication")


            XapiActivity(
                id = activityId,
                definition = XapiActivityDefinition(
                    name = publication.metadata.title.toStringMap(noLangKey = "en-US"),
                    extensions = JsonObject(
                        mapOf(ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK to JsonPrimitive(publicationUrl.toString()))
                    )
                )
            )
        }
    }

    suspend operator fun invoke(
        publications: List<Pair<Publication, Url>>,
    ): List<XapiActivity> {
        return coroutineScope {
            publications.map { (publication, url) ->
                async { invoke(publication = publication, publicationUrl = url) }
            }.awaitAll()
        }
    }

    suspend operator fun invoke(
        feed: OpdsFeed,
        url: Url,
    ) : List<XapiActivity> {
        return invoke(
            publications = feed.allPublications().map { it to url }
        )
    }


    suspend operator fun invoke(selection: PublicationsSelection): List<XapiActivity> {
        return invoke(
            selection.selectedPublications.map {
                val selfLink = it.findSelfLinks().firstOrNull()
                    ?: throw IllegalArgumentException("GetXapiActivityForPublicationUseCase: " +
                            "publication in feed MUST have self link")
                it to selection.url.resolveSafely(selfLink.href)
            }
        )
    }

}