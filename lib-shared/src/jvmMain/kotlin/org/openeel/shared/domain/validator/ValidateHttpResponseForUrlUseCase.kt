package world.respect.shared.domain.validator

import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

/**
 * Validate an HTTP response for a given URL. Can be used to check that the URL:
 * a) Returns a 200/success status code
 * b) Returns a content-type that is acceptable
 * c) Has required headers
 * d) Supports cache validation (e.g. using if-none-match if-modified-since).
 */
class ValidateHttpResponseForUrlUseCase(
    private val httpClient: HttpClient,
) {

    data class ValidateHttpResponseForUrlResult(
        val messages: List<ValidatorMessage>,
        val responseHeaders: Headers?,
        val statusCode: HttpStatusCode?,
    )

    data class ValidationOptions(
        val acceptableMimeTypes: List<String> = emptyList(),
        val checkContentLen: Boolean = true,
        val checkCacheValidation: Boolean = true,
    )

    /**
     * Validate the HTTP response for a given URL (e.g. ensure it returns 200 OK, has required
     * headers, mime type is in acceptable list, etc)
     *
     * @param url url to validate
     * @param referer the referer e.g. the URL from which this was requested - this is shown as the
     *        error source in the reporter.
     */
    suspend operator fun invoke(
        url: String,
        referer: String,
        reporter: ValidatorReporter,
        options: ValidationOptions = DEFAULT_VALIDATION_OPTS,
    ): ValidateHttpResponseForUrlResult  {

        val validatorMessages = mutableListOf<ValidatorMessage>()

        val linkToStr = "Link to $url"
        var responseHeaders: Headers? = null
        var httpStatusCode: HttpStatusCode? = null

        try {
            //As per https://ktor.io/docs/client-responses.html#streaming
            val (lastModified, etag) = httpClient.prepareGet(url){
                expectSuccess = false
                header("accept-encoding", "gzip, deflate, br")
            }.execute { response ->
                responseHeaders = response.headers
                httpStatusCode = response.status

                val contentType = response.headers["content-type"]?.substringBefore(";")
                val lastModHeaderVal = response.headers["last-modified"]
                val etagHeaderVal = response.headers["etag"]

                if(response.status.value != 200) {
                    //Emit only the status error, no point in checking others if response is not 200
                    validatorMessages += reporter.addMessage(
                        ValidatorMessage(
                            level = ValidatorMessage.Level.ERROR,
                            sourceUri = referer,
                            message = "$linkToStr: Response status code not HTTP OK/200. Got: ${response.status.value}"
                        )
                    )
                }else {
                    if(options.acceptableMimeTypes.isNotEmpty() &&
                        contentType !in options.acceptableMimeTypes
                    ) {
                        validatorMessages += reporter.addMessage(
                            ValidatorMessage(
                                level = ValidatorMessage.Level.ERROR,
                                sourceUri = referer,
                                message = "$linkToStr: Response provides unacceptable mime type: $contentType. Should be: ${options.acceptableMimeTypes.joinToString()}"
                            )
                        )
                    }

                    if(options.checkCacheValidation && lastModHeaderVal == null &&
                            etagHeaderVal == null
                    ) {
                        validatorMessages += reporter.addMessage(
                            ValidatorMessage(
                                level = ValidatorMessage.Level.ERROR,
                                sourceUri = referer,
                                message = "$linkToStr: No last-modified or etag header found"
                            )
                        )
                    }

                    val contentLen = response.headers[HttpHeaders.ContentLength]?.toLongOrNull()
                    if(options.checkContentLen && (contentLen == null || contentLen < 0)) {
                        validatorMessages += reporter.addMessage(
                            ValidatorMessage(
                                level = ValidatorMessage.Level.ERROR,
                                sourceUri = referer,
                                message = "$linkToStr: content-length header not specified or not an integer >= 0"
                            )
                        )
                    }
                }

                response.readAndDiscard()

                if(response.status.value == 200) {
                    Pair(lastModHeaderVal, etagHeaderVal)
                }else {
                    Pair(null, null)
                }
            }

            if(etag != null || lastModified != null) {
                httpClient.prepareGet(url) {
                    header("accept-encoding", "gzip, deflate, br")

                    etag?.also { etagVal ->
                        header("If-None-Match", etagVal)
                    }
                    lastModified?.also { lastModVal ->
                        header("If-Modified-Since", lastModVal)
                    }
                }.execute { response ->
                    if(response.status.value != 304) {
                        validatorMessages += reporter.addMessage(
                            ValidatorMessage(
                                level = ValidatorMessage.Level.ERROR,
                                sourceUri = referer,
                                message = buildString {
                                    append("$linkToStr: Did not return 304 not modified when using validation headers: ")
                                    etag?.also {
                                        append("if-none-match=$etag ")
                                    }
                                    lastModified?.also {
                                        append("If-Modified-Since=$lastModified")
                                    }
                                }
                            )
                        )
                    }

                    response.readAndDiscard()
                }
            }
        }catch(e: Throwable) {
            validatorMessages += reporter.addMessage(ValidatorMessage.fromException(url, e))
        }

        return ValidateHttpResponseForUrlResult(
            messages = validatorMessages,
            responseHeaders = responseHeaders,
            statusCode = httpStatusCode,
        )
    }

    companion object {

        val DEFAULT_VALIDATION_OPTS = ValidationOptions()

        val ONLY_CHECK_RESPONSE_IS_SUCCESS = ValidationOptions(
            acceptableMimeTypes = emptyList(),
            checkCacheValidation = false,
            checkContentLen = false,
        )

    }

}