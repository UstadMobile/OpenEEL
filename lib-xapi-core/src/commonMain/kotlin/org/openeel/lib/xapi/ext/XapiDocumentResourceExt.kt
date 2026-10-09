package world.respect.lib.xapi.ext

import io.ktor.util.date.GMTDate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.datetime.toGMTDate
import world.respect.lib.dataloadstate.ext.mapAsync
import world.respect.lib.xapi.resources.ISingleDocumentParams
import world.respect.lib.xapi.resources.XapiDocumentResource
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


