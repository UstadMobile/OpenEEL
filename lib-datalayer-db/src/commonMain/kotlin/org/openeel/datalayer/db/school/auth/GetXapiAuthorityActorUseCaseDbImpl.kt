package org.openeel.datalayer.db.school.auth

import io.ktor.http.Url
import org.openeel.datalayer.school.ext.asXapiAgent
import org.openeel.lib.xapi.auth.GetXapiAuthorityActorUseCase
import org.openeel.lib.xapi.model.XapiActor

class GetXapiAuthorityActorUseCaseDbImpl(
    private val getAuthenticatedPersonUseCase: GetAuthenticatedPersonUseCase,
    private val schoolUrl: Url,
): GetXapiAuthorityActorUseCase {

    override suspend fun invoke(): XapiActor? {
        return getAuthenticatedPersonUseCase()?.asXapiAgent(schoolUrl)
    }
}