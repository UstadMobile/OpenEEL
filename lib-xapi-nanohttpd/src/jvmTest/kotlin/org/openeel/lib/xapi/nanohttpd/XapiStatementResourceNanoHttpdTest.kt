package world.respect.lib.xapi.nanohttpd

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiStatementResourceTest
import org.openeel.libxapi.test.res.SampleXapiStatement
import org.openeel.libxapi.test.res.xapiSampleStatements
import world.respect.lib.xapi.resources.XapiStatementsResource
import kotlin.test.Test
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

class XapiStatementResourceNanoHttpdTest : AbstractXapiStatementResourceTest() {

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
        withNanoHttpdXapiResource(
            dbDir = temporaryFolder.newFolder(),
            json = json,
            httpClient = httpClient,
            authenticatedXapiAgents = { emptyList() }
        ) {
            block(it.statements)
        }
    }

    /**
     * This function exists just to tell Android Studio/IntelliJ that this is a test class,
     * without at least one function annotated test it won't show the run test option
     */
    @Test
    fun thisIsATestClass() {}

}
