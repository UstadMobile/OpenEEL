package world.respect.lib.xapi.extensions.queryresponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

/**
 * Query response as per the xAPI recipe.
 *
 * @param columnNames the column names returned by the query
 * @param rows an array of JsonArray: each array is one row as returned by the query.
 *        Each row array will have the same number of elements as columnNames.
 */
@Serializable
class XapiSqlQueryResponse(
    val columnNames: List<String>,
    val rows: JsonArray,
) {
}