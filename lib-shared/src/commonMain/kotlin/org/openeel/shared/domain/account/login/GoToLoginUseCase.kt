package org.openeel.shared.domain.account.login

import io.ktor.http.Url
import org.openeel.datalayer.respect.model.OpenIdAuthOptionConfig
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.shared.domain.account.authwithopenid.LaunchOpenIdAuthorizationUseCase
import org.openeel.shared.navigation.LoginScreen
import org.openeel.shared.navigation.NavCommand

class GoToLoginUseCase(
    private val schoolUrl: Url,
    private val launchOpenIdAuthorizationUseCase: LaunchOpenIdAuthorizationUseCase,
) {

    suspend operator fun invoke(school: SchoolDirectoryEntry): NavCommand? {
        val openIdConfig = school.authenticationOptions.singleOrNull()
            ?.provider as? OpenIdAuthOptionConfig
        if (openIdConfig != null) {
            launchOpenIdAuthorizationUseCase(openIdConfig)
            return null
        }

        return NavCommand.Navigate(LoginScreen.create(schoolUrl))
    }
}
