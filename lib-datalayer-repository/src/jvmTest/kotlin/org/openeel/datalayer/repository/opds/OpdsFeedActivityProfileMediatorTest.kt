package org.openeel.datalayer.repository.opds

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendEncodedPathSegments
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test

/**
 * Test the [OpdsFeedActivityProfileMediator]
 */
class OpdsFeedActivityProfileMediatorTest {

    private val xapiBaseUrl = Url("http://school.example.org/api/school/xapi")

    val activityId = "http://example.org/feed"

    val expectedParams = XapiActivityProfileResource.SingleDocumentParams(
        activityId = activityId,
        profileId = OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION,
    )

    private fun Url.appendActivityProfile(
        params: XapiActivityProfileResource.SingleDocumentParams
    ) : Url {
       return URLBuilder(this).apply {
           appendEncodedPathSegments("activities", "profile")
           parameters["activityId"] = params.activityId
           parameters["profileId"] = params.profileId
       }.build()
    }


    @Test
    fun givenUrlMatchesActivityProfileResource_whenGetInvoked_thenRequestIsMadeThroughProfileResource() = runBlocking {
        val opdsLocal: OpdsFeedDataSourceLocal = mock()
        val opdsRepo: OpdsFeedDataSource = mock()
        val activityProfileRepo: XapiActivityProfileResource = mock {
            on { get(any(), any()) }.doReturn(NoDataLoadedState.notFound())
        }
        val mediator = OpdsFeedActivityProfileMediator(
            opdsLocal = opdsLocal,
            opdsRepo = opdsRepo,
            activityProfileRepo = activityProfileRepo,
            json = Json { ignoreUnknownKeys = true },
            xapiUrl = { xapiBaseUrl },
        )

        val url = xapiBaseUrl.appendActivityProfile(expectedParams)
        val params = DataLoadParams()

        mediator.getByUrl(url, params)

        verifyBlocking(activityProfileRepo) {
            get(eq(expectedParams), any())
        }
        verifyNoInteractions(opdsRepo)
    }

    @Test
    fun givenUrlMatchesActivityProfileResourceOnOtherServer_whenGetInvoked_thenRequestIsMadeThroughOpdsRepo() = runBlocking {
        val opdsLocal: OpdsFeedDataSourceLocal = mock()
        val opdsRepo: OpdsFeedDataSource = mock {
            on { getByUrl(any(), any()) }.doReturn(NoDataLoadedState.notFound())
        }
        val activityProfileRepo: XapiActivityProfileResource = mock()
        val mediator = OpdsFeedActivityProfileMediator(
            opdsLocal = opdsLocal,
            opdsRepo = opdsRepo,
            activityProfileRepo = activityProfileRepo,
            json = Json { ignoreUnknownKeys = true },
            xapiUrl = { xapiBaseUrl },
        )

        val url = Url("https://otehr.example.org/xapi").appendActivityProfile(expectedParams)
        val params = DataLoadParams()

        mediator.getByUrl(url, params)

        verifyBlocking(opdsRepo) {
            getByUrl(eq(url), eq(params))
        }
        verifyNoInteractions(activityProfileRepo)
    }

    @Test
    fun givenUrlIsNotActivityProfileResourceUrl_whenGetInvoked_thenRequestIsMadeThroughOpdsReop() = runBlocking {
        val opdsLocal: OpdsFeedDataSourceLocal = mock()
        val opdsRepo: OpdsFeedDataSource = mock {
            on { getByUrl(any(), any()) }.doReturn(NoDataLoadedState.notFound())
        }
        val activityProfileRepo: XapiActivityProfileResource = mock()
        val mediator = OpdsFeedActivityProfileMediator(
            opdsLocal = opdsLocal,
            opdsRepo = opdsRepo,
            activityProfileRepo = activityProfileRepo,
            json = Json { ignoreUnknownKeys = true },
            xapiUrl = { xapiBaseUrl },
        )

        val url = Url("http://school.example.org/other/opds/feed.json")
        val params = DataLoadParams()

        mediator.getByUrl(url, params)

        verifyBlocking(opdsRepo) {
            getByUrl(eq(url), eq(params))
        }
        verifyNoInteractions(activityProfileRepo)
    }

}