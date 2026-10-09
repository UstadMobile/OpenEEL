package world.respect.shared.domain.xapi.getxapilaunchurl

import io.ktor.http.Url
import world.respect.lib.opds.model.Publication

/**
 * Get the Url to launch a specific learning unit. This should include Xapi Launch parameters (
 * eg actor, endpoint etc). When launching in a webview, this will use the local embedded server.
 *
 * Add Rustici launch method parameters as per:
 * https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 */
interface GetXapiLaunchUrlUseCase {

    enum class LaunchType {
        NATIVE, WEBVIEW
    }

    suspend operator fun invoke(
        publication: Publication,
        publicationUrl: Url,
        assignmentActivityId: String?,
        type: LaunchType,
    ): Url

}