package world.respect.shared.domain.launchapp.getlaunchoptionsforpublication

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import nl.adaptivity.xmlutil.serialization.XML
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findLaunchableAppLink
import world.respect.lib.opds.model.findLearningUnitAcquisitionLinks
import world.respect.lib.opds.model.findTinCanXmlLink
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.libutil.ext.resolve
import world.respect.shared.util.ext.legacyActivityIdForLink

/**
 * Get a list of launch options that can be used for a given publication. Will look for a link to
 * tincan.xml if specified.
 */
class GetLaunchOptionsForPublicationUseCase(
    private val httpClient: HttpClient,
    private val xml: XML,
    private val opdsPublicationDataSource: OpdsPublicationDataSource,
) {

    enum class LaunchType {

        LEGACY, XAPI_RUSTICI_LAUNCH, NO_XAPI

    }

    data class GetLaunchOptionsResult(
        val options: List<LaunchOption>,
        val launchableApp: Publication?,
    )

    data class LaunchOption(
        val url: Url,
        val activityId: String,
        val launchType: LaunchType,
    )

    suspend operator fun invoke(
        publication: Publication,
        publicationUrl: Url,
    ): GetLaunchOptionsResult {
        val launchOptions = mutableListOf<LaunchOption>()
        val tinCanLink = publication.findTinCanXmlLink()
        val launchableAppLink = publication.findLaunchableAppLink()?.let { link ->
            opdsPublicationDataSource.getByUrl(
                url = publicationUrl.resolve(link.href),
                params = DataLoadParams(),
            )
        }

        val tinCanXmlUrl = tinCanLink?.let { publicationUrl.resolve(it.href) }

        tinCanXmlUrl?.also {
            val tinCanXmlContent = httpClient.get(tinCanXmlUrl).bodyAsText()

            xml.decodeFromString(
                TinCanXmlDocument.serializer(), tinCanXmlContent
            ).activities.activity.forEach {
                it.launch?.value?.also { launchHref ->
                    launchOptions.add(
                        LaunchOption(
                            url = tinCanXmlUrl.resolve(launchHref),
                            activityId = it.id,
                            launchType = LaunchType.XAPI_RUSTICI_LAUNCH,
                        )
                    )
                }
            }
        }

        publication.findLearningUnitAcquisitionLinks().forEach { link ->
            val linkUrl = publicationUrl.resolve(link.href)
            launchOptions.add(
                LaunchOption(
                    url = linkUrl,
                    activityId = publication.legacyActivityIdForLink(link, publicationUrl),
                    launchType = LaunchType.LEGACY,
                )
            )
        }

        return GetLaunchOptionsResult(
            options = launchOptions.toList(),
            launchableApp = launchableAppLink?.dataOrNull(),
        )
    }

}