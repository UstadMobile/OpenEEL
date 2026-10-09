package world.respect.xapi.ipc.server

import androidx.test.rule.ServiceTestRule
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.Test
import org.openeel.libxapi.test.AbstractXapiStateResourceTest
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.resources.XapiStateResource

class XapiStateResourceIpcTest : AbstractXapiStateResourceTest() {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private val json = Json {
        encodeDefaults = false
    }

    override suspend fun withXapiDocumentResource(
        authenticatedAgents: GetAuthenticatedXapiAgentsUseCase,
        block: suspend (XapiStateResource) -> Unit
    ) {
        withXapiResourceIpcTest(
            serviceRule = serviceRule,
            json = json,
            authenticatedAgents = authenticatedAgents,
        ) {
            block(it.state)
        }
    }

    /**
     * This function exists just to tell Android Studio/IntelliJ that this is a test class,
     * without at least one function annotated test it won't show the run test option
     */
    @Test
    fun thisIsATestClass() {
        //givenDocumentStoredAndNotModified_whenRetrievedWithValidationHeaders_thenReturnsNotModified()
    }
}
