package world.respect.lib.xapi.resources.local

import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import kotlin.uuid.Uuid

interface XapiStatementsResourceLocal: XapiStatementsResource{

    suspend fun getByUuid(uuid: Uuid): XapiStatement?

    suspend fun updateLocal(
        list: List<XapiStatement>,
    )

}
