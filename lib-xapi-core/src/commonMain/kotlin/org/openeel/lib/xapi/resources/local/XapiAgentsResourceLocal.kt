package org.openeel.lib.xapi.resources.local

import org.openeel.lib.xapi.model.XapiActor
import org.openeel.lib.xapi.resources.XapiAgentsResource
import kotlin.time.Instant

interface XapiAgentsResourceLocal : XapiAgentsResource {

    suspend fun updateLocal(
        actors: List<XapiActor>,
        timestamp: Instant,
    )

}