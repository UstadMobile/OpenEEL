package world.respect.lib.xapi.rusticilaunch

import nl.adaptivity.xmlutil.serialization.XML
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import kotlin.test.Test
import kotlin.test.assertEquals

class TinCanXmlDocumentTest {

    @Test
    fun givenValidTinCanXml_whenDecoded_thenValuesAsExpected() {
        val xml = XML.v1 {
            recommended_1_0_0()
        }

        val tinCanXml = xml.decodeFromString(
            TinCanXmlDocument.serializer(), TINCAN_GOLF_EXAMPLE
        )

        val golfActivity = tinCanXml.activities.activity.first()
        assertEquals("Tin Can Tetris Example", golfActivity.name)
        assertEquals(
            expected = "http://id.tincanapi.com/activity/tincan-prototypes/tetris",
            actual = golfActivity.id
        )
        assertEquals("A game of Tetris", golfActivity.description?.value)
        assertEquals("en-US", golfActivity.description?.lang)
        assertEquals("tetris.html", golfActivity.launch?.value)
    }


    companion object {

        //As per https://github.com/RusticiSoftware/TinCan_Prototypes/blob/master/JsTetris_TCAPI/tincan.xml
        private const val TINCAN_GOLF_EXAMPLE = """
            <tincan xmlns="http://projecttincan.com/tincan.xsd">
                <activities>
                    <activity id="http://id.tincanapi.com/activity/tincan-prototypes/tetris" type="http://activitystrea.ms/schema/1.0/game">
                        <name>Tin Can Tetris Example</name>
                        <description lang="en-US">A game of Tetris</description>
                        <launch lang="en-us">tetris.html</launch>
                    </activity>
                </activities>
            </tincan>
        """

    }
}