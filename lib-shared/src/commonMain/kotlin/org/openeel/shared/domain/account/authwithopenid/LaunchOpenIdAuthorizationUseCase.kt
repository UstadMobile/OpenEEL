package org.openeel.shared.domain.account.authwithopenid

import org.openeel.datalayer.respect.model.OpenIdAuthOptionConfig

/**
 * Start OpenID authorization: on Android this uses AppAuth Android to launch a Custom Chrome Tab
 * to complete authorization and redirect on completion.
 */
interface LaunchOpenIdAuthorizationUseCase {

    suspend operator fun invoke(config: OpenIdAuthOptionConfig)

}
