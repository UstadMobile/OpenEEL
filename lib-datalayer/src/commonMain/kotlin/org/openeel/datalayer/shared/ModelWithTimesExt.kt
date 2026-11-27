package org.openeel.datalayer.shared

import org.openeel.lib.directory.ModelWithTimes
import kotlin.time.Instant

fun <T: ModelWithTimes> List<T>.maxLastStoredOrNull(): Instant? {
    return maxOfOrNull { it.stored }
}

fun <T: ModelWithTimes> List<T>.maxLastModifiedOrNull(): Instant? {
    return maxOfOrNull { it.lastModified }
}
