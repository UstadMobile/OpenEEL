package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable

/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#25-retrieval-of-statements
 */
@Serializable
data class XapiStatementResult(
    @Serializable(with = XapiSingleItemToListSerializer::class)
    val statements: List<XapiStatement>,
    val more: String?
)
