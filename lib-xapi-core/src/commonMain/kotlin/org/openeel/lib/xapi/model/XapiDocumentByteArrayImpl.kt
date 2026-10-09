package world.respect.lib.xapi.model

import io.ktor.util.date.GMTDate

class XapiDocumentByteArrayImpl(
    override val type: String,
    override val updated: GMTDate,
    val contents: ByteArray,
) : XapiDocument {
    override suspend fun contentsAsByteArray(): ByteArray = contents
}