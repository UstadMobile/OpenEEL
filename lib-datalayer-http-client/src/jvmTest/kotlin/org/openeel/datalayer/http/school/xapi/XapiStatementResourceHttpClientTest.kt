package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiStatementResourceTest
import org.openeel.libxapi.test.res.SampleXapiStatement
import world.respect.datalayer.http.server.XapiStatementsResourceRoute
import world.respect.datalayer.school.model.AuthToken
import org.openeel.libxapi.test.res.xapiSampleStatements
import world.respect.lib.test.clientservertest.withEmbeddedDataSourceServer
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.test.Test
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient


class XapiStatementResourceHttpClientTest: AbstractXapiStatementResourceTest() {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    }


    override fun loadXapiSampleStatements(): List<SampleXapiStatement> = xapiSampleStatements()

    override suspend fun withXapiStatementResource(block: suspend (XapiStatementsResource) -> Unit) {
        withEmbeddedDataSourceServer(
            dbDir = temporaryFolder.newFolder(),
            routingConfig = { context ->
                XapiStatementsResourceRoute(
                    statementResource = {
                        context.datasourceContext.datasource.xapiResource.statements
                    },
                    json = json,
                )
            }
        ) {
            val statementResource = XapiStatementsResourceHttpClient(
                xapiUrl = { schoolUrl },
                httpClient = httpClient,
                tokenProvider = {
                    AuthToken("secret", systemTimeInMillis(), 3600)
                },
                json = json,
            )

            block(statementResource)
        }
    }

    /**
     * This function exists just to tell Android Studio/IntelliJ that this is a test class,
     * without at least one function annotated test it won't show the run test option
     */
    @Test
    fun thisIsATestClass() {}

}