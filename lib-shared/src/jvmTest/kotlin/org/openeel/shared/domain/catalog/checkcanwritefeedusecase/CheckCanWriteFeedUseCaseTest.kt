package org.openeel.shared.domain.catalog.checkcanwritefeedusecase

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.runBlocking
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.mockito.kotlin.mock
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.lib.xapi.OpenEelXapiConstants
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckCanWriteFeedUseCaseTest {

    val teacherPerson = Person(
        guid = "1",
        roles = listOf(
            PersonRole(isPrimaryRole = true, roleEnum = PersonRoleEnum.TEACHER),
        ),
        givenName = "Teacher",
        familyName = "User",
        gender = PersonGenderEnum.FEMALE,
    )

    val studentPerson = Person(
        guid = "2",
        roles = listOf(
            PersonRole(isPrimaryRole = true, roleEnum = PersonRoleEnum.STUDENT),
        ),
        givenName = "Student",
        familyName = "User",
        gender = PersonGenderEnum.FEMALE,
    )

    val adminPerson = Person(
        guid = "3",
        roles = listOf(
            PersonRole(isPrimaryRole = true, roleEnum = PersonRoleEnum.SYSTEM_ADMINISTRATOR),
        ),
        givenName = "Admin",
        familyName = "User",
        gender = PersonGenderEnum.MALE,
    )

    val siteAdminPerson = Person(
        guid = "4",
        roles = listOf(
            PersonRole(isPrimaryRole = true, roleEnum = PersonRoleEnum.SITE_ADMINISTRATOR),
        ),
        givenName = "SiteAdmin",
        familyName = "User",
        gender = PersonGenderEnum.MALE,
    )

    val xapiBaseUrl = Url("https://school.example.org/xapi/")

    val profileFeedUrl = URLBuilder(xapiBaseUrl).apply {
        appendPathSegments("activities/profile")
        encodedParameters["activityId"] = UrlEncoderUtil.encode("https://school.example.org/feeds/uid")
        encodedParameters["profileId"] = OpenEelXapiConstants.ACTIVITY_PROFILEID_OPDS_COLLECTION
    }.build()

    val otherProfileFeedUrl = URLBuilder(xapiBaseUrl).apply {
        appendPathSegments("activities/profile")
        encodedParameters["activityId"] = UrlEncoderUtil.encode("https://school.example.org/feeds/uid")
        encodedParameters["profileId"] = "other"
    }.build()

    val externalFeedUrl = Url("https://example.app/collection.json")


    @Test
    fun givenFeedIsActivityProfileBasedAndUserIsTeacher_whenInvoked_thenReturnsTrue() = runBlocking {
        assertTrue(
            CheckCanWriteFeedUseCase(
                getAuthenticatedPersonUseCase = mock {
                    on { invoke() }.thenReturn(teacherPerson)
                },
                xapiUrl = { xapiBaseUrl }
            ).invoke(profileFeedUrl)
        )
    }

    @Test
    fun givenFeedIsActivityProfileBasedAndUserIsNotTeacher_whenInvoked_thenReturnsTrue() = runBlocking {
        assertFalse(
            CheckCanWriteFeedUseCase(
                getAuthenticatedPersonUseCase = mock {
                    on { invoke() }.thenReturn(studentPerson)
                },
                xapiUrl = { xapiBaseUrl }
            ).invoke(profileFeedUrl)
        )
    }

    @Test
    fun givenFeedIsNotActivityProfileBasedAndUserIsTeacher_whenInvoked_thenReturnsFalse() = runBlocking {
        assertFalse(
            CheckCanWriteFeedUseCase(
                getAuthenticatedPersonUseCase = mock {
                    on { invoke() }.thenReturn(teacherPerson)
                },
                xapiUrl = { xapiBaseUrl }
            ).invoke(otherProfileFeedUrl)
        )
    }

    @Test
    fun givenFeedIsExternal_whenInvoked_thenReturnsFalse() = runBlocking {
        assertFalse(
            CheckCanWriteFeedUseCase(
                getAuthenticatedPersonUseCase = mock {
                    on { invoke() }.thenReturn(teacherPerson)
                },
                xapiUrl = { xapiBaseUrl }
            ).invoke(externalFeedUrl)
        )
    }

    @Test
    fun givenFeedIsActivityProfileBasedAndUserIsSystemAdmin_whenInvoked_thenReturnsTrue() = runBlocking {
        assertTrue(
            CheckCanWriteFeedUseCase(
                getAuthenticatedPersonUseCase = mock {
                    on { invoke() }.thenReturn(adminPerson)
                },
                xapiUrl = { xapiBaseUrl }
            ).invoke(profileFeedUrl)
        )
    }

    @Test
    fun givenFeedIsActivityProfileBasedAndUserIsSiteAdmin_whenInvoked_thenReturnsTrue() = runBlocking {
        assertTrue(
            CheckCanWriteFeedUseCase(
                getAuthenticatedPersonUseCase = mock {
                    on { invoke() }.thenReturn(siteAdminPerson)
                },
                xapiUrl = { xapiBaseUrl }
            ).invoke(profileFeedUrl)
        )
    }

    @Test
    fun givenFeedIsActivityProfileBasedAndUserIsNull_whenInvoked_thenReturnsFalse() = runBlocking {
        assertFalse(
            CheckCanWriteFeedUseCase(
                getAuthenticatedPersonUseCase = mock {
                    on { invoke() }.thenReturn(null)
                },
                xapiUrl = { xapiBaseUrl }
            ).invoke(profileFeedUrl)
        )
    }


}