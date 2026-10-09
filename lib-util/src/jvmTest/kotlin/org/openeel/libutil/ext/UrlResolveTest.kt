package world.respect.libutil.ext

import io.ktor.http.Url
import org.junit.Test
import kotlin.test.assertEquals

class UrlResolveTest {


    /**
     * Basic peace of mind test scenarios
     */
    @Test
    fun testResolution() {
        assertEquals("https://server.ustadmobile.app/",
            Url("https://server.ustadmobile.app/somewhere/").resolve("../").toString())
        assertEquals("https://www.google.com/",
            Url("https://server.ustadmobile.app/somewhere/")
                .resolve("https://www.google.com/").toString())
        assertEquals("https://server.ustadmobile.app/somewhere/link.json",
            Url("https://server.ustadmobile.app/somewhere/")
                .resolve("link.json").toString())
    }


    @Test
    fun testAppendEndpointSegments() {
        val endpoint = Url("https://school.example.org/api/school/xapi")
        val endpointWithSlash = Url("https://school.example.org/api/school/xapi/")

        assertEquals("https://school.example.org/api/school/xapi/statements",
            endpoint.appendEndpointSegments(listOf("statements")).toString())
        assertEquals("https://school.example.org/api/school/xapi/statements",
            endpointWithSlash.appendEndpointSegments(listOf("statements")).toString())

        assertEquals("https://school.example.org/api/school/xapi/actor/profile",
            endpoint.appendEndpointSegments(listOf("actor", "profile")).toString())
        assertEquals("https://school.example.org/api/school/xapi/actor/profile",
            endpointWithSlash.appendEndpointSegments(listOf("actor", "profile")).toString())

        assertEquals("https://school.example.org/api/school/xapi/actor/profile",
            endpoint.appendEndpointSegments(listOf("actor/profile")).toString())
        assertEquals("https://school.example.org/api/school/xapi/actor/profile",
            endpointWithSlash.appendEndpointSegments(listOf("actor/profile")).toString())
    }

    @Test
    fun testEndpointNormalization() {
        assertEquals("https://school.example.org/",
            Url("https://school.example.org/").normalizeForEndpoint().toString())

        assertEquals("https://school.example.org/",
            Url("https://school.example.org").normalizeForEndpoint().toString())

        assertEquals("https://school.example.org/",
            Url("https://school.EXAMPLE.org/").normalizeForEndpoint().toString())

        assertEquals("https://school.example.org/subpath/",
            Url("https://school.example.org/subpath").normalizeForEndpoint().toString())

        assertEquals("https://school.example.org/subpath/",
            Url("https://school.example.org/subpath/").normalizeForEndpoint().toString())
    }


}