package org.openeel.datalayer.db.school

import io.ktor.http.Url
import org.openeel.datalayer.school.ext.asXapiAgent
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.model.XapiAgent

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