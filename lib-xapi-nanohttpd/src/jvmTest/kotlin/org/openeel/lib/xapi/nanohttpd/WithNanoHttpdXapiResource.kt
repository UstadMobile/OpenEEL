package org.openeel.lib.xapi.nanohttpd

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.datalayer.http.school.xapi.XapiResourceHttpClient
import org.openeel.datalayer.school.model.AuthToken
import org.openeel.lib.test.clientservertest.insertAdminAndDefaultGrants
import org.openeel.lib.test.clientservertest.withSchoolDbDataSource
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.libutil.findFreePort
import org.openeel.libutil.util.time.systemTimeInMillis
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