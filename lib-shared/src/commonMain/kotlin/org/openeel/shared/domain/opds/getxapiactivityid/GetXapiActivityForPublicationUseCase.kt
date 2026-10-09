package world.respect.shared.domain.opds.getxapiactivityid

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.adaptivity.xmlutil.serialization.XML
import world.respect.datalayer.school.opds.ext.requireAbsoluteSelfUrl
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findLearningUnitAcquisitionLinks
import world.respect.lib.opds.model.findTinCanXmlLink
import world.respect.lib.opds.model.toStringMap
import world.respect.lib.xapi.OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.libutil.ext.resolve
import world.respect.shared.util.ext.legacyActivityIdForLink

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