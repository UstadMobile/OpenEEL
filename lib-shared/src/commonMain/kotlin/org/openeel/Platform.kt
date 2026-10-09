package org.openeel

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform