package world.respect.shared.domain.account.passkey

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.libutil.ext.appendEndpointSegments

class GetActivePersonPasskeysClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
) : GetActivePersonPasskeysUseCase {

    override suspend fun getActivePeronPasskeys(personGuid: String): List<PersonPasskeyEntity> {
        val result=httpClient.get(
            schoolUrl.appendEndpointSegments("api/passkey/allactivepasskeys")
        ) {
            contentType(ContentType.Application.Json)
            parameter("personGuid", personGuid)
        }
        println("slls"+result.bodyAsText())
        println("slls"+result.body())
       return result.body()
    }
}