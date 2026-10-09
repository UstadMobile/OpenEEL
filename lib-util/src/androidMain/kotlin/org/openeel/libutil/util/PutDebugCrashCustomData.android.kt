package org.openeel.libutil.util

import org.acra.ACRA

actual fun putDebugCrashCustomData(key: String, value: String) {
    ACRA.takeIf { it.isInitialised }?.errorReporter?.putCustomData(key, value)
}