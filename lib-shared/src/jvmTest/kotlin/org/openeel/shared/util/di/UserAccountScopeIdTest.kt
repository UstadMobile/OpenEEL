package org.openeel.shared.util.di

import io.ktor.http.Url
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import kotlin.test.Test
import kotlin.test.assertEquals

class UserAccountScopeIdTest {

    @Test
    fun givenSchoolIdAndAccountId_whenParsed_thenShouldMatch() {
        val accountScopeId = UserAccountScopeId(
            Url("https://school.example.org/", ),
            AuthenticatedUserPrincipalId("12")
        )

        assertEquals(accountScopeId,
            UserAccountScopeId.parse(accountScopeId.scopeId)
        )
    }

}