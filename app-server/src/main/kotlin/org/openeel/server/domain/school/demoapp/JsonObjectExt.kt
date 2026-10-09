package world.respect.server.domain.school.demoapp

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.requireString(key: String) : String {
    return get(key)?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("requireString: no key $key on object")
}
