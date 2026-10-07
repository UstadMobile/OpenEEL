package org.openeel.datalayer.ext

fun <T: Any> List<T>.appendIfNotNull(
    other: List<T>?
): List<T> {
    return if(other != null) {
        this + other
    }else {
        this
    }
}