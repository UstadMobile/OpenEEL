package org.openeel.xapi.ipc.shared.messages

import android.os.Bundle
import io.ktor.http.Parameters
import org.openeel.xapi.ipc.shared.messages.ext.BundleStringValues

class BundleParameters(
    bundle: Bundle
): BundleStringValues(bundle, caseInsensitiveName = false), Parameters
