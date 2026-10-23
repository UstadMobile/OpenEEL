package org.openeel.lib.xapi.permissionmanager.policy

import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.resources.XapiStatementsResource

interface XapiStatementsResourcePolicy {

    suspend fun transformReadParams(
        params: XapiStatementsResource.GetStatementParams
    ): XapiStatementsResource.GetStatementParams

    suspend fun transformWrite(
        statement: XapiStatement
    ): XapiStatement

}