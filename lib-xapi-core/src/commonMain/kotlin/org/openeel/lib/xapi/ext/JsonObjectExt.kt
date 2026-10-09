package world.respect.lib.xapi.ext

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlin.collections.component1
import kotlin.collections.component2

fun JsonObjectBuilder.putAllExcept(
    other: JsonObject,
    exceptKeys: List<String>
) {
    other.entries.forEach { (key, value) ->
        if(key !in exceptKeys) {
            put(key, value)
        }
    }
}

fun JsonObjectBuilder.putAll(
    other: JsonObject
) {
    other.entries.forEach {
        put(it.key, it.value)
    }
}

/**
 * Merge this JsonObject (the receiver) with another Json object (the parameter). Merge ONLY the
 * top level as per xAPI spec on merging documents.
 */
fun JsonObject.mergeTopLevel(
    other: JsonObject
): JsonObject {
    return JsonObject(
        content = this.toMutableMap().also { map ->
            other.forEach { (key, value) ->
                map[key] = value
            }
        }.toMap()
    )
}


