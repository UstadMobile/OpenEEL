package org.openeel.lib.xapi.resources

import org.openeel.lib.xapi.XapiRequestHeaders
import org.openeel.lib.xapi.model.XapiActor


/**
 *
 */
interface XapiAgentsResource {

    suspend fun getPerson(
        actor: XapiActor,
        xapiRequestHeaders: XapiRequestHeaders,
    )

}