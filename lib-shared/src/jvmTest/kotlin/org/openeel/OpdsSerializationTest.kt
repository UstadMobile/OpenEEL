package world.respect

import kotlinx.serialization.json.Json
import world.respect.lib.opds.model.OpdsFeed
import kotlin.test.Test

class OpdsSerializationTest {

    @Test
    fun contributorSerializerTest() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val testStr = this::class.java.getResourceAsStream("/opds-io-home.json")!!.bufferedReader()
            .use { it.readText() }
        val opds = json.decodeFromString(OpdsFeed.serializer(), testStr)
        println(opds)
    }

}