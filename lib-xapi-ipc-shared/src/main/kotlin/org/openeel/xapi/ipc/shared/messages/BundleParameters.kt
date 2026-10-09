package world.respect.xapi.ipc.shared.messages

import android.os.Bundle
import io.ktor.http.Parameters
import world.respect.xapi.ipc.shared.messages.ext.BundleStringValues

class BundleParameters(
    bundle: Bundle
): BundleStringValues(bundle, caseInsensitiveName = false), Parameters
