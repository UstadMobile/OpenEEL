package world.respect.shared.domain.launchapp

import io.ktor.http.Url
import world.respect.lib.opds.model.Publication

/**
 * Interface to launch a RESPECT compatible app.
 */
interface LaunchAppUseCase {

    /**
     * @param publicationUrl The URL from which the publication is loaded
     * @param publication the publication to open: this can be a learning unit or compatible app.
     * @param assignmentActivityId if a learning unit id is being launched as part of an assignment,
     *        the assignmentActivityId. This will be used by the embedded xAPI server to modify
     *        statements received to follow the assignment recipe (add assignmentActivityId to
     *        contextActivities).
     * @param launchableApp if the launchable app publication is already cached/available in
     *        memory e.g. as is the case when the app is launched from the publication detail
     *        screen, then this should be provided to avoid needing to go back to the datasource.
     */
    data class LaunchAppRequest(
        val publicationUrl: Url,
        val publication: Publication,
        val assignmentActivityId: String? = null,
        val launchableApp: Publication? = null,
    )

    sealed class LaunchAppResult

    object LaunchAppSuccess: LaunchAppResult()

    class LaunchAppInstallRequired(
        val launchableApp: Publication?,
        val referrerUrl: Url,
    ): LaunchAppResult()

    class LaunchAppFailed(
        val cause: Throwable?
    ): LaunchAppResult()

    suspend operator fun invoke(
        request: LaunchAppRequest
    ): LaunchAppResult

    companion object {

        //As per integration guide
        const val RESPECT_LAUNCH_VERSION_PARAM_NAME = "respectLaunchVersion"

        const val RESPECT_LAUNCH_VERSION_VALUE = "1"

    }

}