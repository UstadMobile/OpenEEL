package world.respect.domain.opds.validator

import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import world.respect.shared.domain.validator.ValidatorMessage
import world.respect.shared.domain.validator.ValidatorReporter

suspend fun HttpClient.verifyMimeTypeAndGetBodyAsText(
    url: String,
    acceptableMimeTypes: List<String>,
    reporter: ValidatorReporter,
): String {
    return prepareGet(url){
        expectSuccess = false
    }.execute { response ->
        val contentType = response.headers["content-type"]?.substringBefore(";")
        if(contentType !in acceptableMimeTypes) {
            reporter.addMessage(
                ValidatorMessage(
                    level = ValidatorMessage.Level.ERROR,
                    sourceUri = url,
                    message = "Mime type is not application/json",
                )
            )
        }

        if(!response.status.isSuccess()) {
            reporter.addMessage(
                ValidatorMessage(
                    level = ValidatorMessage.Level.ERROR,
                    sourceUri = url,
                    message = "HTTP status: failure: ${response.status}")
            )
        }

        response.bodyAsText()
    }
}