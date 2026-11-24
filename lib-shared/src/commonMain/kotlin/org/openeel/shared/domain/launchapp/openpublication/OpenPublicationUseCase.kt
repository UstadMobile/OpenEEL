package org.openeel.shared.domain.launchapp.openpublication

import io.ktor.http.Url
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.Publication
import org.openeel.shared.domain.launchapp.LaunchAppUseCase
import org.openeel.shared.domain.launchapp.gotoappstore.GoToAppStoreUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.app_not_found
import org.openeel.shared.util.exception.withUiText
import org.openeel.shared.util.ext.asUiText

/**
 * Open a publication (e.g. lesson) as per the Open button on the publication detail screen. If the
 * required app is not installed, go to the app store.
 */
class OpenPublicationUseCase(
    private val launchAppUseCase: LaunchAppUseCase,
    private val goToAppStoreUseCase: GoToAppStoreUseCase,
    private val opdsPublicationDataSource: OpdsPublicationDataSource,
) {

    /**
     * @param publication the publication if already loaded, otherwise it will be loaded from
     *        publicationUrl
     */
    data class OpenPublicationRequest(
        val publicationUrl: Url,
        val publication: Publication? = null,
        val assignmentActivityId: String? = null,
        val launchableApp: Publication? = null,
    )

    suspend operator fun invoke(request: OpenPublicationRequest) {
        val publication = request.publication ?: opdsPublicationDataSource.getByUrl(
            url = request.publicationUrl,
            params = DataLoadParams(),
        ).dataOrNull() ?: throw IllegalStateException("Could not load ${request.publicationUrl}")

        val result = launchAppUseCase(
            LaunchAppUseCase.LaunchAppRequest(
                publicationUrl = request.publicationUrl,
                publication = publication,
                assignmentActivityId = request.assignmentActivityId,
                launchableApp = request.launchableApp,
            )
        )

        return when(result) {
            LaunchAppUseCase.LaunchAppSuccess -> Unit

            is LaunchAppUseCase.LaunchAppInstallRequired -> {
                val launchableApp = result.launchableApp ?: throw IllegalStateException()
                    .withUiText(Res.string.app_not_found.asUiText())

                goToAppStoreUseCase(
                    GoToAppStoreUseCase.Request(
                        launchableApp = launchableApp,
                        referrer = result.referrerUrl.toString(),
                    )
                )
            }

            is LaunchAppUseCase.LaunchAppFailed -> throw result.cause
                ?: IllegalStateException("Could not launch ${request.publicationUrl}")
        }
    }

}
