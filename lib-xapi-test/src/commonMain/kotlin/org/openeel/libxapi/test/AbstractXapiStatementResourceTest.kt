package org.openeel.libxapi.test

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.openeel.libxapi.test.res.SampleXapiStatement
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.throwable.unwrapHttpStatusCode
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementRef
import org.openeel.lib.xapi.model.XapiStatementResult
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.lib.xapi.resources.XapiStatementsResource.GetStatementFormatEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.uuid.Uuid

abstract class AbstractXapiStatementResourceTest {

    abstract suspend fun withXapiStatementResource(
        block: suspend (XapiStatementsResource) -> Unit
    )

    abstract fun loadXapiSampleStatements(): List<SampleXapiStatement>

    val json = Json

    @Test
    fun givenStatement_whenStoredAndRetrieved_thenShouldMatch() = runBlocking {
        loadXapiSampleStatements().forEach { statement ->
            withXapiStatementResource { resource ->
                val stmtUuid = Uuid.random()
                val timeNow = Clock.System.now()

                val statement = Json.decodeFromJsonElement(
                    XapiStatement.serializer(), statement.jsonObject
                ).copy(
                    id = stmtUuid,
                    timestamp = timeNow,
                    stored = Clock.System.now(),
                )

                resource.post(listOf(statement))

                //check canonical match
                val canonicalStmtFromDb = resource.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        format = GetStatementFormatEnum.CANONICAL,
                        statementId = stmtUuid
                    )
                ).dataOrNull()?.statements?.first()

                assertNotNull(canonicalStmtFromDb)
                assertXapiStatementCanonicallyEqual(
                    expected = statement,
                    actual = canonicalStmtFromDb,
                )

                val exactStmtFromResource = resource.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        format = GetStatementFormatEnum.EXACT,
                        statementId = stmtUuid
                    )
                ).dataOrNull()?.statements?.first()
                assertEquals(statement, exactStmtFromResource)

                val idOnlyStmtFromDb = resource.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        format = GetStatementFormatEnum.IDS,
                        statementId = stmtUuid
                    )
                ).dataOrNull()?.statements?.first()
                assertNotNull(idOnlyStmtFromDb)
                assertXapiStatementCanonicallyEqual(
                    expected = statement,
                    actual = idOnlyStmtFromDb,
                    idOnlyFormat = true,
                )
            }
        }
    }


    @Test
    fun givenStatementInserted_whenVoided_thenShouldNotBeInGetResults() =runBlocking {
        val sampleStmt = loadXapiSampleStatements().first()
        val stmtUuid = Uuid.random()
        val statement = Json.decodeFromJsonElement(
            XapiStatement.serializer(), sampleStmt.jsonObject
        ).copy(id = stmtUuid)

        withXapiStatementResource { resource ->
            val stmtUuid = statement.id ?: throw IllegalArgumentException("Statement id to be stored must not be null")

            val getStmtParams = XapiStatementsResource.GetStatementParams(
                statementId = stmtUuid
            )

            resource.post(listOf(statement))

            assertXapiStatementCanonicallyEqual(
                expected = statement,
                actual = resource.get(
                    listParams = getStmtParams
                ).dataOrNull()?.statements?.firstOrNull()!!
            )

            val voidingStatement = XapiStatement(
                actor = statement.actor,
                verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                `object` = XapiStatementRef(id = stmtUuid.toString())
            )
            resource.post(listOf(voidingStatement))

            GetStatementFormatEnum.entries.forEach { format ->
                assertNull(
                    resource.get(
                        listParams = getStmtParams.copy(
                            format = format
                        )
                    ).dataOrNull()?.statements?.firstOrNull()
                )
            }

            val getByVoidedParams = XapiStatementsResource.GetStatementParams(
                voidedStatementId = stmtUuid
            )
            assertEquals(
                expected = statement,
                actual = resource.get(
                    getByVoidedParams.copy(format = GetStatementFormatEnum.EXACT)
                ).dataOrNull()?.statements?.firstOrNull()
            )
            assertXapiStatementCanonicallyEqual(
                expected = statement,
                actual = resource.get(
                    getByVoidedParams.copy(format = GetStatementFormatEnum.CANONICAL)
                ).dataOrNull()?.statements?.firstOrNull()!!
            )
        }
    }


    @Test
    fun givenStatementPosted_whenAnotherStatementPostedWithSameId_thenShouldReturnErrorState() = runBlocking {
        withXapiStatementResource { resource ->
            val stmtUuid = Uuid.random()
            val sampleStmt = json.decodeFromJsonElement(
                XapiStatement.serializer(), loadXapiSampleStatements().first().jsonObject
            ).copy(
                id = stmtUuid
            )

            resource.post(listOf(sampleStmt))
            val loadStateAfterPost = resource.post(listOf(sampleStmt))
            assertIs<DataErrorResult<XapiStatementResult>>(loadStateAfterPost)
            val exception = loadStateAfterPost.error
            assertEquals(409, exception.unwrapHttpStatusCode())
        }
    }


}