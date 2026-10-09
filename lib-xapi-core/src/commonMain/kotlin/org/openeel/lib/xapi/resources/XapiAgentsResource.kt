package world.respect.lib.xapi.resources

import world.respect.lib.xapi.XapiRequestHeaders
import world.respect.lib.xapi.model.XapiActor


/**
 *
 */
interface XapiAgentsResource {

    suspend fun getPerson(
        actor: XapiActor,
        xapiRequestHeaders: XapiRequestHeaders,
    )

}