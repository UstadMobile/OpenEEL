package world.respect.lib.xapi.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.lib.serializers.InstantAsISO8601
import world.respect.lib.serializers.SingleItemToListTransformer
import world.respect.lib.xapi.ext.putAllExcept
import kotlin.uuid.Uuid

const val XAPI_RESULT_EXTENSION_PROGRESS = "https://w3id.org/xapi/cmi5/result/extensions/progress"

//This extension is wrong, but it is used by Articulate content
const val XAPI_RESULT_EXTENSION_PROGRESS_NON_HTTPS = "http://w3id.org/xapi/cmi5/result/extensions/progress"

val XAPI_PROGRESSED_EXTENSIONS = listOf(
    XAPI_RESULT_EXTENSION_PROGRESS, XAPI_RESULT_EXTENSION_PROGRESS_NON_HTTPS
)


/**
 * XapiStatement represents both a Statement and a SubStatement, therefor it implements the sealed
 * interface XapiStatementObject
 *
 * @property objectType ObjectType is only found on a SubStatement, not a statement.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = XapiStatementTransformingSerializer::class)
@KeepGeneratedSerializer
data class XapiStatement(
    val id: Uuid? = null,
    val actor: XapiActor,
    val verb: XapiVerb,
    @SerialName("object")
    val `object`: XapiStatementObject,
    val result: XapiResult? = null,
    val context: XapiContext? = null,
    val timestamp: InstantAsISO8601? = null,
    val stored: InstantAsISO8601? = null,
    val authority: XapiActor? = null,
    val version: String? = null,
    val attachments: List<XapiAttachment>? = null,
    override val objectType: XapiObjectType? = null,
): XapiStatementObject


/**
 * Transforming serializer that will handle the difference between a statement and a substatement.
 *
 * When Deserializing: the serializer will determine whether or not the object is a SubStatement
 * based on whether it has an objectType value of SubStatement (as required by the xAPI spec for
 * SubStatements).
 *
 * As per:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#substatements
 *
 */
object XapiStatementTransformingSerializer: JsonTransformingSerializer<XapiStatement>(
    XapiStatement.generatedSerializer()
) {

    private val statementExcludedProperties = listOf("objectType")

    private val substatementExcludedProperties = listOf("id", "stored", "authority", "version")

    override fun transformSerialize(element: JsonElement): JsonElement {
        val jsonObject = element as JsonObject
        return buildJsonObject {
            val objectType = jsonObject["objectType"]?.jsonPrimitive?.contentOrNull
            putAllExcept(
                other = jsonObject,
                exceptKeys = if(objectType == XapiObjectType.SubStatement.value) {
                    substatementExcludedProperties
                }else {
                    statementExcludedProperties
                }
            )
        }
    }

}

/**
 * Handle receiving post requests for Xapi Statements:
 */
object XapiSingleItemToListSerializer: SingleItemToListTransformer<XapiStatement>(
    XapiStatementTransformingSerializer
)


