package world.respect.datalayer.ext

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.etag
import io.ktor.http.fromHttpToGmtDate
import io.ktor.http.isSuccess
import io.ktor.http.lastModified
import io.ktor.util.reflect.TypeInfo
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.throwable.HttpErrorResponseException
import world.respect.lib.dataloadstate.throwable.withHttpStatus
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import kotlin.time.Instant

fun HttpResponse.dataLoadMetaInfo(
    validationInfoKey: Long = 0
): DataLoadMetaInfo{
    val varyHeader = headers.getAll(HttpHeaders.Vary)
        ?.joinToString(separator = ",")

    return DataLoadMetaInfo(
        url = request.url,
        lastModified = lastModifiedAsLong(),
        etag = etag(),
        consistentThrough = consistentThrough(),
        varyHeader = varyHeader,
        permissionsLastModified = permissionsLastModified(),
        validationInfoKey = validationInfoKey,
        headers = headers,
    )
}


suspend fun <T: Any> HttpResponse.toDataLoadState(
    typeInfo: TypeInfo,
    validationInfoKey: Long = 0,
): DataLoadState<T> {
    return toDataLoadState(
        validationInfoKey = validationInfoKey,
        bodyAdapter = {
            body(typeInfo)
        }
    )
}

/**
 * Convert the Http Body to a XapiDocument using the bodyAsBytes() function
 */
suspend fun HttpResponse.bodyAsXapiDocument(): XapiDocument {
    return XapiDocumentByteArrayImpl(
        type = headers[HttpHeaders.ContentType] ?: "application/octet-stream",
        updated = headers[HttpHeaders.LastModified]?.fromHttpToGmtDate()
            ?: throw IllegalArgumentException("bodyAsXapiDocument: No Last-Modified header found in response"),
        contents = bodyAsBytes()
    )
}

suspend fun HttpResponse.toXapiDocumentDataLoadState() : DataLoadState<XapiDocument>{
    return toDataLoadState(
        bodyAdapter = {
            it.bodyAsXapiDocument()
        }
    )
}

suspend fun <T: Any> HttpResponse.toDataLoadState(
    validationInfoKey: Long = 0,
    bodyAdapter: suspend (HttpResponse) -> T
): DataLoadState<T> {
    val metaInfo = dataLoadMetaInfo(validationInfoKey = validationInfoKey)
    return when {
        status == HttpStatusCode.NotModified -> {
            NoDataLoadedState.notModified(metaInfo = metaInfo)
        }

        status == HttpStatusCode.NotFound -> {
            NoDataLoadedState.notFound(metaInfo = metaInfo)
        }

        status.isSuccess() -> {
            DataReadyState(
                data = bodyAdapter(this),
                metaInfo = metaInfo
            )
        }

        else -> {
            DataErrorResult(
                error = HttpErrorResponseException(status.value, status.description),
                metaInfo = metaInfo,
            )
        }
    }
}

