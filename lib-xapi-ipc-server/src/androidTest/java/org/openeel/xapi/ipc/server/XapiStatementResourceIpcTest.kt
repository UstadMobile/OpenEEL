package world.respect.xapi.ipc.server

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import org.junit.Rule
import org.junit.Test
import org.openeel.libxapi.test.AbstractXapiStatementResourceTest
import org.openeel.libxapi.test.res.SampleXapiStatement
import org.openeel.libxapi.test.res.xapiSampleStatements
import world.respect.lib.xapi.resources.XapiStatementsResource

class XapiStatementResourceIpcTest : AbstractXapiStatementResourceTest() {

    @get:Rule
    val serviceRule = ServiceTestRule()

    override fun loadXapiSampleStatements(): List<SampleXapiStatement> {
        return xapiSampleStatements(InstrumentationRegistry.getInstrumentation().context)
    }

    override suspend fun withXapiStatementResource(
        block: suspend (XapiStatementsResource) -> Unit
    ) {
        withXapiResourceIpcTest(
            serviceRule = serviceRule,
            json = json,
            authenticatedAgents = { emptyList() },
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
