package world.respect.datalayer.school.ext

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder

fun JsonObjectBuilder.putAll(
    other: JsonObject
) {
    other.entries.forEach {
        put(it.key, it.value)
    }
}