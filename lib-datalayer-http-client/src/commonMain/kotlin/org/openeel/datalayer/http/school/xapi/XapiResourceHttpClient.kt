package org.openeel.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.lib.xapi.resources.XapiActivitiesResource
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiAgentsResource
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.lib.xapi.resources.XapiStatementsResource

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