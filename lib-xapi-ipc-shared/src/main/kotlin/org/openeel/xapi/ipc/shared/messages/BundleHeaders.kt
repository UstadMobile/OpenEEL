package org.openeel.xapi.ipc.shared.messages

import android.os.Bundle
import io.ktor.http.Headers
import org.openeel.xapi.ipc.shared.messages.ext.BundleStringValues

class BundleHeaders(
    bundle: Bundle
) : BundleStringValues(bundle, caseInsensitiveName = true), Headers
