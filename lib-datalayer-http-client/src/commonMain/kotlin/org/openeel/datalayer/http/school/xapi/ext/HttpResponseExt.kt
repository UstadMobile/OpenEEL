package world.respect.datalayer.http.school.xapi.ext

import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import world.respect.lib.xapi.exceptions.XapiException

/**
 * Throw a [XapiException] if the response status is not successful.
 */
fun HttpResponse.throwXapiExceptionIfNotSuccessful() {
    if(!status.isSuccess())
        throw XapiException(status.value, status.description)
}
