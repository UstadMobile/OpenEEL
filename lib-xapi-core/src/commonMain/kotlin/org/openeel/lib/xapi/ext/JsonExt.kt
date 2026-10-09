package org.openeel.lib.xapi.ext

import io.ktor.util.date.GMTDate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.model.XapiDocumentByteArrayImpl
import kotlin.time.Clock

fun <T: Any> Json.encodeToXapiDocument(
    serializer: SerializationStrategy<T>,
    value: T,
    updated: GMTDate = Clock.System.now().toGMTDate(),
): XapiDocument {
    return XapiDocumentByteArrayImpl(
        type = "application/json",
        updated = updated,
        contents = encodeToString(serializer, value).encodeToByteArray()
    )
}

suspend fun <T: Any> Json.decodeFromXapiDocument(
    deserializer: DeserializationStrategy<T>,
    document: XapiDocument,
) : T {
    return decodeFromString(
        deserializer = deserializer, document.contentsAsByteArray().decodeToString()
    )
}
