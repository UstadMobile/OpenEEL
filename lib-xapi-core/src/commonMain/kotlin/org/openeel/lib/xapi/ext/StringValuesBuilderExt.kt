package world.respect.lib.xapi.ext

import io.ktor.util.StringValuesBuilder
import kotlin.uuid.Uuid

fun StringValuesBuilder.setIfNotNull(
    name: String,
    value: String?
) {
    if(value != null)
        set(name, value)
}

fun StringValuesBuilder.setIfNotNull(
    name: String,
    uuid: Uuid?
) {
    if(uuid != null)
        set(name, uuid.toString())
}

