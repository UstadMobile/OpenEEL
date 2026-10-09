package world.respect.datalayer.shared

import kotlin.time.Instant

fun <T: ModelWithTimes> List<T>.maxLastStoredOrNull(): Instant? {
    return maxOfOrNull { it.stored }
}

fun <T: ModelWithTimes> List<T>.maxLastModifiedOrNull(): Instant? {
    return maxOfOrNull { it.lastModified }
}
