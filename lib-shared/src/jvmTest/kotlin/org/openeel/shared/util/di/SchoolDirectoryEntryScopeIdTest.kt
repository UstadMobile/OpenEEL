package org.openeel.shared.util.di

import io.ktor.http.Url
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import kotlin.test.Test
import kotlin.test.assertEquals

class SchoolDirectoryEntryScopeIdTest {

    @Test
    fun givenSchoolIdAndAccountId_whenParsed_thenShouldMatch() {
        val schoolScopeId = SchoolDirectoryEntryScopeId(
            Url("https://school.example.org/"),
            AuthenticatedUserPrincipalId("12")
        )

        assertEquals(schoolScopeId,
            SchoolDirectoryEntryScopeId.parse(schoolScopeId.scopeId))
    }

    @Test
    fun givenSchoolIdAndNullAccount_whenParsed_thenShouldMatch() {
        val schoolScopeId = SchoolDirectoryEntryScopeId(
            Url("https://school.example.org/"), null
        )

        assertEquals(schoolScopeId,
            SchoolDirectoryEntryScopeId.parse(schoolScopeId.scopeId))
    }

}