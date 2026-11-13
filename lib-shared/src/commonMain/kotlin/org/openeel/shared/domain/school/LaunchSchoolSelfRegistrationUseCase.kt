package org.openeel.shared.domain.school

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.shared.domain.appversioninfo.GetAppVersionInfoUseCase

/**
 * Start the school self-registration process for the given directory (host) by opening the
 * directory's register-school page in a custom tab.
 */
class LaunchSchoolSelfRegistrationUseCase(
    private val launchCustomTabUseCase: LaunchCustomTabUseCase,
    private val getAppVersionInfoUseCase: GetAppVersionInfoUseCase,
) {

    suspend operator fun invoke(directoryUrl: Url) {
        launchCustomTabUseCase(
            url = URLBuilder(
                directoryUrl.appendEndpointSegments(REGISTER_SCHOOL_PATH)
            ).apply {
                parameters.append("packageName", getAppVersionInfoUseCase().packageName)
            }.build()
        )
    }

    companion object {

        const val REGISTER_SCHOOL_PATH = "school-directory/register-school"

    }
}
