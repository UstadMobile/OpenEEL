package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import kotlin.uuid.Uuid

suspend fun XapiStatementsResource.put(
    statementId: Uuid,
    statement: XapiStatement
) {
    post(
        listOf(statement.copy(id = statementId))
    )
}
