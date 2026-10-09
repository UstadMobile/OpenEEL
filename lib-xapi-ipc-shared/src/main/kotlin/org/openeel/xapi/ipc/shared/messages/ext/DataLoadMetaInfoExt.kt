package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import io.ktor.http.Headers
import world.respect.lib.dataloadstate.DataLoadMetaInfo

fun DataLoadMetaInfo.toBundle(): Bundle {
    return (headers ?: Headers.Empty).toBundle()
}
