package org.openeel.domain.school.add

import io.ktor.http.parametersOf
import org.openeel.shared.domain.school.add.RegisterSchoolUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegisterSchoolUseCaseTest {

    @Test
    fun givenSubdomainParams_whenFromParamsCalled_thenUrlIsAsExpected() {
        val params = parametersOf(
            RegisterSchoolUseCase.PARAM_SUBDOMAIN_PARENT to listOf("example.org"),
            RegisterSchoolUseCase.PARAM_SUBDOMAIN to listOf("schoolname"),
            RegisterSchoolUseCase.PARAM_SUBDOMAIN_PROTO to listOf("https"),
        )

        val request = RegisterSchoolUseCase.RegisterSchoolRequest.fromParameters(params)

        assertEquals("https://schoolname.example.org", request.schoolUrl)
    }

    @Test
    fun givenPackageNameParam_whenFromParamsCalled_thenPackageNameIsSet() {
        val params = parametersOf(
            RegisterSchoolUseCase.PARAM_SCHOOL_NAME to listOf("School"),
            RegisterSchoolUseCase.PARAM_SCHOOL_FULL_URL to listOf("https://school.example.org/"),
            RegisterSchoolUseCase.PARAM_PACKAGE_NAME to listOf("org.example.app"),
        )

        val request = RegisterSchoolUseCase.RegisterSchoolRequest.fromParameters(params)

        assertEquals("org.example.app", request.packageName)
    }

    @Test
    fun givenNoPackageNameParam_whenFromParamsCalled_thenPackageNameIsNull() {
        val params = parametersOf(
            RegisterSchoolUseCase.PARAM_SCHOOL_NAME to listOf("School"),
            RegisterSchoolUseCase.PARAM_SCHOOL_FULL_URL to listOf("https://school.example.org/"),
        )

        assertNull(RegisterSchoolUseCase.RegisterSchoolRequest.fromParameters(params).packageName)
    }

}