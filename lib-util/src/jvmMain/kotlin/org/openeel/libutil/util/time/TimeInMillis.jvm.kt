package org.openeel.libutil.util.time

actual fun systemTimeInMillis(): Long {
    return System.currentTimeMillis()
}
