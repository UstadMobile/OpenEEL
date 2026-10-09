package world.respect.lib.xapi.resources.local

import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.resources.XapiAgentsResource
import kotlin.time.Instant

interface XapiAgentsResourceLocal : XapiAgentsResource {

    suspend fun updateLocal(
        actors: List<XapiActor>,
        timestamp: Instant,
    )

}