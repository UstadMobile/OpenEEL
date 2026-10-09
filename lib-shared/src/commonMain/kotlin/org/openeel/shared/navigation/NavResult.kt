package org.openeel.shared.navigation

import org.openeel.libutil.util.time.systemTimeInMillis

data class NavResult(
    val key: String,
    val timestamp: Long = systemTimeInMillis(),
    val result: Any?
)