package world.respect.lib.xapi.ext

import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import io.ktor.util.StringValues
import kotlin.uuid.Uuid

fun StringValues.getUuidOrNull(
    name: String
): Uuid? {
    return get(name)?.let { Uuid.parse(it) }
}

fun StringValues.toParametersFormUrlEncoded() : String {
    return Parameters.build {
        appendAll(this@toParametersFormUrlEncoded)
    }.formUrlEncode()
}

