package org.openeel.libutil.util

expect fun <T> concurrentSafeListOf(vararg items: T) : MutableList<T>