package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementObject
import world.respect.lib.xapi.model.XapiStatementRef

fun XapiStatementObject.idAsStringOrNull() : String? {
    return when(this) {
        is XapiActivity -> id
        is XapiStatement -> null
        is XapiGroup -> this.idStr
        is XapiAgent -> idStr
        is XapiStatementRef -> id
    }
}
