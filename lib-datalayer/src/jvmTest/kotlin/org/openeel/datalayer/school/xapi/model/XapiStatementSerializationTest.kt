package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.json.Json
import org.openeel.libxapi.test.res.forXapiSampleStatements
import world.respect.lib.xapi.model.XapiStatement
import kotlin.test.Test

class XapiStatementSerializationTest {

    @Test
    fun givenValidStatements_whenDeserialized_shouldLoadWithoutException() {
        forXapiSampleStatements { sampleStmt ->
            Json.decodeFromJsonElement(XapiStatement.serializer(), sampleStmt.jsonObject)
        }
    }
}