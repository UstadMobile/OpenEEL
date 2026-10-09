package world.respect.datalayer.school.xapi.ext

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import world.respect.datalayer.school.ext.putAll
import kotlin.uuid.Uuid



fun JsonObject.addStatementIdIfNotPresent(): JsonObject {
    return if(!containsKey("id")) {
        buildJsonObject {
            putAll(this@addStatementIdIfNotPresent)
            put("id", JsonPrimitive(Uuid.random().toString()))
        }
    }else {
        this
    }
}
