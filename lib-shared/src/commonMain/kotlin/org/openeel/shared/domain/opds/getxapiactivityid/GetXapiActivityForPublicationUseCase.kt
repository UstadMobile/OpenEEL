package org.openeel.shared.domain.opds.getxapiactivityid

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.adaptivity.xmlutil.serialization.XML
import org.openeel.datalayer.school.opds.ext.requireAbsoluteSelfUrl
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.findLearningUnitAcquisitionLinks
import org.openeel.lib.opds.model.findTinCanXmlLink
import org.openeel.lib.opds.model.toStringMap
import org.openeel.lib.xapi.OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiActivityDefinition
import org.openeel.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import org.openeel.libutil.ext.resolve
import org.openeel.shared.util.ext.legacyActivityIdForLink

/**
 * Get an XapiActivity (including definition) for a given publication. If the publication includes
 * a link to a tincan.xml file, that will be used. Otherwise, the publication's identifier will be
 * used. See README_XAPI_OPDS.md
 */
class GetXapiActivityForPublicationUseCase(
    private val xml: XML,
    private val httpClient: HttpClient,
) {

    suspend operator fun invoke(
        publication: Publication
    ) : XapiActivity {
        val publicationUrl = publication.requireAbsoluteSelfUrl()

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
        publications: List<Publication>
    ): List<XapiActivity> {
        return coroutineScope {
            publications.map { publication ->
                async { invoke(publication) }
            }.awaitAll()
        }
    }


}