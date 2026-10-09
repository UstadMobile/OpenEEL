package world.respect.libutil.util

expect fun <T> concurrentSafeListOf(vararg items: T) : MutableList<T>