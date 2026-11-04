package org.openeel.libxapi.test

import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.lib.xapi.resources.XapiStatementsResource.GetStatementFormatEnum
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * Asserts that the given [statement] can be successfully stored in the provided [resource]
 * and subsequently retrieved across different statement formats ([GetStatementFormatEnum.CANONICAL],
 * [GetStatementFormatEnum.EXACT], and [GetStatementFormatEnum.IDS]).
 *
 * Verifies that:
 * - The statement can be posted to the [resource].
 * - Retrieving with [GetStatementFormatEnum.CANONICAL] returns a statement canonically equal to [statement].
 * - Retrieving with [GetStatementFormatEnum.EXACT] returns a statement exactly equal to [statement].
 * - Retrieving with [GetStatementFormatEnum.IDS] returns a statement canonically equal to [statement] in ID-only format.
 *
 * @param statement The [XapiStatement] to store and verify. Must have a non-null [XapiStatement.id].
 * @param resource The [XapiStatementsResource] used to store and retrieve the statement.
 */
suspend fun assertStatementCanBeStoredAndRetrieved(
    statement: XapiStatement,
    resource: XapiStatementsResource
) {
    val stmtUuid = statement.id
        ?: throw IllegalArgumentException("Statement id to be stored must not be null")

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

    val exactStmtFromDb = resource.get(

        listParams = XapiStatementsResource.GetStatementParams(
            format = GetStatementFormatEnum.EXACT,
            statementId = stmtUuid
        )
    ).dataOrNull()?.statements?.first()
    assertEquals(statement, exactStmtFromDb)

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

