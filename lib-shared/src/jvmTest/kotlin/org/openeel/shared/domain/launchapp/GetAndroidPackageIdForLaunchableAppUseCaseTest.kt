package world.respect.shared.domain.launchapp

import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpenEelConstants
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetAndroidPackageIdForLaunchableAppUseCaseTest {

    private val useCase = GetAndroidPackageIdForLaunchableAppUseCase()

    private val basePublication = Publication(
        metadata = ReadiumMetadata(title = LangMapStringValue("Test App")),
        links = emptyList()
    )

    private fun assertPublicationWithPackageIdMatches(
        packageId: String?,
        link: String,
    ) {
        assertEquals(
            expected = packageId,
            actual = useCase(
                basePublication.copy(
                    links = listOf(
                        ReadiumLink(
                            href = link,
                            rel = listOf(OpenEelConstants.REL_ANDROID_APP_STORE)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun googlePlayUrlReturnsPackageId() {
        assertPublicationWithPackageIdMatches(
            "demo.openeel.org",
            "https://play.google.com/store/apps/details?id=demo.openeel.org"
        )
    }

    @Test
    fun marketUrlReturnsPackageId() {
        assertPublicationWithPackageIdMatches(
            "demo.openeel.org",
            "market://details?id=demo.openeel.org"
        )
    }

    @Test
    fun noPackageIdReturnsNull() {
        assertPublicationWithPackageIdMatches(
            null,
            "https://google.com/"
        )
    }

    @Test
    fun assertMalformedUrlReturnsNull() {
        assertPublicationWithPackageIdMatches(
            null,
            "'?;"
        )
    }


}
