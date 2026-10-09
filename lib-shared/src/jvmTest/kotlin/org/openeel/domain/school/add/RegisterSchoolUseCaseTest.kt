package world.respect.domain.school.add

import io.ktor.http.parametersOf
import world.respect.shared.domain.school.add.RegisterSchoolUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

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

}