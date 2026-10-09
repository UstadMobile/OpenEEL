package world.respect.datalayer.db.school

import io.ktor.http.Url
import world.respect.datalayer.school.ext.asXapiAgent
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.model.XapiAgent

class GetAuthenticatedXapiAgentsUseCaseDbImpl(
    private val getAuthenticatedPersonUseCase: GetAuthenticatedPersonUseCase,
    private val schoolUrl: Url,
): GetAuthenticatedXapiAgentsUseCase {

    override suspend fun invoke(): List<XapiAgent> {
        return getAuthenticatedPersonUseCase()?.asXapiAgent(schoolUrl)?.let {
            listOf(it)
        }.orEmpty()
    }

}