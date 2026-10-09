package world.respect.lib.opds.model

import kotlinx.serialization.json.Json
import kotlin.test.Test

class OpdsSerializerTest {

    @Test
    fun givenInputData_whenDeserialized_thenMatches() {
        val jsonStr = this::class.java.getResourceAsStream("/lesson001.json")!!.use {
            it.bufferedReader().readText()
        }
        val opdsPub = Json.decodeFromString<Publication>(jsonStr)
    }

}