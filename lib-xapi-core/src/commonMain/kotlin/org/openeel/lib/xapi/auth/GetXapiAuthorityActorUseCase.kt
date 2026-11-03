package org.openeel.lib.xapi.auth

import org.openeel.lib.xapi.model.XapiActor

/**
 * Get the [XapiActor] as would be set for the [org.openeel.lib.xapi.model.XapiStatement.authority].
 */
fun interface GetXapiAuthorityActorUseCase {

    suspend operator fun invoke(): XapiActor?

}