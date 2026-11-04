package org.openeel.lib.xapi.ext

import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.resources.XapiStatementsResource
import kotlin.uuid.Uuid

suspend fun XapiStatementsResource.put(
    statementId: Uuid,
    statement: XapiStatement
) {
    post(
        listOf(statement.copy(id = statementId))
    )
}
