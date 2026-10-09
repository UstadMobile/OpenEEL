package world.respect.xapi.ipc.shared.messages

import android.os.Bundle
import io.ktor.http.Headers
import world.respect.xapi.ipc.shared.messages.ext.BundleStringValues

class BundleHeaders(
    bundle: Bundle
) : BundleStringValues(bundle, caseInsensitiveName = true), Headers
