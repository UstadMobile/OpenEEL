package world.respect.server.domain.school.demoapp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.openeel.demo.demolaunchableappserver.DemoConstants

class DemoStringMaps(
    val langMaps: Map<String, JsonObject>
) {

    fun requireLangMap(lang: String) : JsonObject {
        return langMaps[lang] ?: throw IllegalArgumentException("$lang not found")
    }

    fun requireString(lang: String, key: String) : String {
        return requireLangMap(lang)[key]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("$key not found")
    }


    companion object {

        fun initFromResources(
            basePath: String = "/demoapp/locales/",
            json: Json
        ): DemoStringMaps {
            return DemoStringMaps(
                langMaps = DemoConstants.LANGUAGE_CODES.associateWith { langName ->
                    DemoStringMaps::class.java.getResourceAsStream("$basePath$langName.json")!!
                        .reader().readText().let { jsonStr ->
                            json.decodeFromString(JsonObject.serializer(), jsonStr)
                        }
                }
            )
        }

    }
}

