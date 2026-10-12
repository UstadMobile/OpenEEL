package org.openeel.datalayer.ext

import io.ktor.http.HttpMessage
import io.ktor.http.lastModified
import org.openeel.lib.dataloadstate.DataLayerHeaders
import kotlin.time.Instant

fun HttpMessage.lastModifiedAsLong(): Long {
    return lastModified()?.time ?: -1
}

fun HttpMessage.consistentThrough(): Instant? {
    return headers[DataLayerHeaders.XConsistentThrough]?.let {
        Instant.parse(it)
    }
}

fun HttpMessage.permissionsLastModified(): Instant? {
    return headers[DataLayerHeaders.XPermissionsLastModified]?.let { Instant.parse(it) }
}

