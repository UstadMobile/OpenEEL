package org.openeel.lib.xapi.ext

import io.ktor.util.date.GMTDate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.dataloadstate.ext.mapAsync
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource
import kotlin.time.Clock

suspend fun <
    SingleDocParams: ISingleDocumentParams<*>,
    T: Any
> XapiDocumentResource<*, SingleDocParams>.putJson(
    docParams: SingleDocParams,
    document: T,
    json: Json,
    serializer: SerializationStrategy<T>,
    updated: GMTDate = Clock.System.now().toGMTDate(),
) {
    put(
        params = docParams,
        document = json.encodeToXapiDocument(
            updated = updated,
            serializer = serializer,
            value = document,
        )
    )
}

suspend fun <
    SingleDocParams: ISingleDocumentParams<*>,
    T: Any
> XapiDocumentResource<*, SingleDocParams>.postJson(
    docParams: SingleDocParams,
    document: T,
    json: Json,
    serializer: SerializationStrategy<T>,
    updated: GMTDate = Clock.System.now().toGMTDate(),
) {
    post(
        params = docParams,
        document = json.encodeToXapiDocument(
            updated = updated,
            serializer = serializer,
            value = document,
        )
    )
}

suspend fun <
    SingleDocParams: ISingleDocumentParams<*>,
    T: Any
> XapiDocumentResource<*, SingleDocParams>.getJson(
    docParams: SingleDocParams,
    json: Json,
    deserializer: DeserializationStrategy<T>,
): DataLoadState<T> {
    return get(
        params = docParams,
    ).mapAsync { document ->
        json.decodeFromXapiDocument(
            deserializer = deserializer,
            document = document,
        )
    }
}


