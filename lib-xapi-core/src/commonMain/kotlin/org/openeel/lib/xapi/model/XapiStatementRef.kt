package world.respect.lib.xapi.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import world.respect.lib.xapi.ext.putAll


/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#object-is-statement
 */
@Serializable(with = XapiStatementRefTransformingSerializer::class)
@KeepGeneratedSerializer
data class XapiStatementRef(
    override val objectType: XapiObjectType = XapiObjectType.StatementRef,
    val id: String,
): XapiStatementObject

/**
 * As per the Xapi Spec: the objectType on a StatementRef is required, and it must be "StatementRef".
 *
 * If encodeDefaults is set to false (default), then this property would be omitted.
 */
object XapiStatementRefTransformingSerializer: JsonTransformingSerializer<XapiStatementRef>(
    XapiStatementRef.generatedSerializer()
) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        return buildJsonObject {
            putAll(element as JsonObject)
            put("objectType", JsonPrimitive(XapiObjectType.StatementRef.value))
        }
    }
}

