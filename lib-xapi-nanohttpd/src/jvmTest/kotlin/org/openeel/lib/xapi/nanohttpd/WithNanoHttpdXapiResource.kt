package world.respect.lib.xapi.nanohttpd

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.http.school.xapi.XapiResourceHttpClient
import world.respect.datalayer.school.model.AuthToken
import world.respect.lib.test.clientservertest.insertAdminAndDefaultGrants
import world.respect.lib.test.clientservertest.withSchoolDbDataSource
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiResource
import world.respect.libutil.findFreePort
import world.respect.libutil.util.time.systemTimeInMillis
import java.io.File

suspend fun withNanoHttpdXapiResource(
    dbDir: File,
    json: Json,
    httpClient: HttpClient,
    authenticatedXapiAgents: GetAuthenticatedXapiAgentsUseCase,
    block: suspend (XapiResource) -> Unit
) {
    val port = findFreePort()
    val schoolUrl = Url("http://localhost:$port/")

    withSchoolDbDataSource(
        dbDir = dbDir,
        schoolUrl = schoolUrl,
        getAuthenticatedXapiAgentsUseCase = authenticatedXapiAgents,
    ) {
        datasource.insertAdminAndDefaultGrants(db)

        val app = XapiNanoHttpdApp(
            port = port,
            json = json,
            xapiResourceProvider = { _, _ ->
                datasource.xapiResource
            },
        ).also {
            it.start()
        }

        try {
            val localUrl = app.localUrlForEndpoint(schoolUrl)

            val xapiResource = XapiResourceHttpClient(
                xapiUrl = { localUrl },
                httpClient = httpClient,
                tokenProvider = {
                    AuthToken("secret", systemTimeInMillis(), 3600)
                },
                json = json,
            )

            block(xapiResource)
        } finally {
            app.stop()
        }
    }
}