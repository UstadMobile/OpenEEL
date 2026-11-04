package org.openeel.lib.xapi.ext

import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiGroup
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementObject
import org.openeel.lib.xapi.model.XapiStatementRef

fun XapiStatementObject.idAsStringOrNull() : String? {
    return when(this) {
        is XapiActivity -> id
        is XapiStatement -> null
        is XapiGroup -> this.idStr
        is XapiAgent -> idStr
        is XapiStatementRef -> id
    }
}
