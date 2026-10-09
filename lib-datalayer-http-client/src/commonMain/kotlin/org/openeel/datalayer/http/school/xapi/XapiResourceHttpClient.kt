package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.lib.xapi.resources.XapiActivitiesResource
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.lib.xapi.resources.XapiAgentsResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStateResource
import world.respect.lib.xapi.resources.XapiStatementsResource

class XapiResourceHttpClient(
    private val xapiUrl: suspend () -> Url,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
): XapiResource {

    override val statements: XapiStatementsResource by lazy {
        XapiStatementsResourceHttpClient(
            xapiUrl = xapiUrl,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            json = json,
        )
    }

    override val agents: XapiAgentsResource
        get() = TODO("Not yet implemented")

    override val activities: XapiActivitiesResource
        get() = TODO("Not yet implemented")

    override val state: XapiStateResource by lazy {
        XapiStateResourceHttpClient(
            xapiUrl = xapiUrl,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            json = json,
        )
    }

    override val activityProfile: XapiActivityProfileResource by lazy {
        XapiActivityProfileResourceHttpClient(
            xapiUrl = xapiUrl,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
        )
    }

    override val agentProfile: XapiAgentProfileResource by lazy {
        XapiAgentProfileResourceHttpClient(
            xapiUrl = xapiUrl,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            json = json,
        )
    }

    override fun close() {
        //Does nothing yet
    }
}