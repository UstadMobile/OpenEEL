package org.openeel.lib.xapi.resources.local

import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.resources.XapiStatementsResource
import kotlin.uuid.Uuid

interface XapiStatementsResourceLocal: XapiStatementsResource{

    suspend fun getByUuid(uuid: Uuid): XapiStatement?

    suspend fun updateLocal(
        list: List<XapiStatement>,
    )

}
