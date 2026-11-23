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
import org.openeel.shared.util.ext.resolve

/**
 * Open a publication (e.g. a lesson) as per the Open button on the publication detail screen:
 * launch it using [LaunchAppUseCase], or go to the app store if the required app is not installed.
 *
 * Throws an exception (with a UiText where available) if the publication cannot be opened.
 */
class OpenPublicationUseCase(
    private val launchAppUseCase: LaunchAppUseCase,
    private val goToAppStoreUseCase: GoToAppStoreUseCase,
    private val opdsPublicationDataSource: OpdsPublicationDataSource,
) {

    /**
     * @param publicationUrl the URL of the publication manifest
     * @param publication the publication (with links resolved against [publicationUrl]) if already
     *        loaded, otherwise it will be loaded from [publicationUrl].
     * @param expectedPublicationId the expected publication identifier, used when loading the
     *        publication.
     * @param assignmentActivityId as per [LaunchAppUseCase.LaunchAppRequest.assignmentActivityId]
     * @param launchableApp as per [LaunchAppUseCase.LaunchAppRequest.launchableApp]
     */
    data class OpenPublicationRequest(
        val publicationUrl: Url,
        val publication: Publication? = null,
        val expectedPublicationId: String? = null,
        val assignmentActivityId: String? = null,
        val launchableApp: Publication? = null,
    )

    suspend operator fun invoke(request: OpenPublicationRequest) {
        return when(
            val result = launchAppUseCase(
                LaunchAppUseCase.LaunchAppRequest(
                    publicationUrl = request.publicationUrl,
                    publication = request.publication ?: opdsPublicationDataSource.getByUrl(
                        url = request.publicationUrl,
                        params = DataLoadParams(),
                        referrerUrl = request.publicationUrl,
                        expectedPublicationId = request.expectedPublicationId,
                    ).dataOrNull()?.resolve(request.publicationUrl)
                        ?: throw IllegalStateException("Could not load publication ${request.publicationUrl}"),
                    assignmentActivityId = request.assignmentActivityId,
                    launchableApp = request.launchableApp,
                )
            )
        ) {
            LaunchAppUseCase.LaunchAppSuccess -> Unit

            is LaunchAppUseCase.LaunchAppInstallRequired -> goToAppStoreUseCase(
                GoToAppStoreUseCase.Request(
                    launchableApp = result.launchableApp ?: throw IllegalStateException(
                        "App install required for ${request.publicationUrl}, but no launchable app"
                    ).withUiText(Res.string.app_not_found.asUiText()),
                    referrer = result.referrerUrl.toString(),
                )
            )

            is LaunchAppUseCase.LaunchAppFailed -> throw result.cause
                ?: IllegalStateException("Could not launch ${request.publicationUrl}")
        }
    }

}
