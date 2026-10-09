package world.respect.shared.util.di

import io.ktor.http.Url
import world.respect.datalayer.AuthenticatedUserPrincipalId
import kotlin.test.Test
import kotlin.test.assertEquals

class RespectAccountScopeIdTest {

    @Test
    fun givenSchoolIdAndAccountId_whenParsed_thenShouldMatch() {
        val accountScopeId = RespectAccountScopeId(
            Url("https://school.example.org/", ),
            AuthenticatedUserPrincipalId("12")
        )

        assertEquals(accountScopeId,
            RespectAccountScopeId.parse(accountScopeId.scopeId)
        )
    }

}