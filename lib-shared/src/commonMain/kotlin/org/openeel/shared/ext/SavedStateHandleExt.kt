package org.openeel.shared.ext

import androidx.lifecycle.SavedStateHandle

/**
 * Same as getOrPut on Map
 */
inline fun <T: Any> SavedStateHandle.getOrPut(
    key: String,
    defaultValue: () -> T
): T {
    return get(key) ?: defaultValue().also {
        this[key] = it
    }
}