package world.respect.shared.navigation

import world.respect.libutil.util.time.systemTimeInMillis

data class NavResult(
    val key: String,
    val timestamp: Long = systemTimeInMillis(),
    val result: Any?
)