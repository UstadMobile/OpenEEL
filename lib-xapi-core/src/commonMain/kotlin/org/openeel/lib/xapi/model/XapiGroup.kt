package world.respect.lib.xapi.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import world.respect.lib.xapi.ext.putAll

/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 *
 * Note: the member list may be returned in any order as per the xAPI spec, so we can reorder it to
 * produce a consistent hash
 *
 * @property member as per the xAPI spec the member property is optional for identified groups.
 * @property objectType as per the xAPI spec https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 *           the objectType on a group is required, and it must be "group".
 */
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = XapiGroupTransformingSerializer::class)
data class XapiGroup(
    override val name: String? = null,
    override val mbox: String? = null,
    override val mbox_sha1sum: String? = null,
    override val openid: String? = null,
    override val objectType: XapiObjectType = XapiObjectType.Group,
    override val account: XapiAccount? = null,
    val member: List<XapiAgent>? = null,
): XapiActor, XapiStatementObject {

    init {
        if(objectType != XapiObjectType.Group)
            throw IllegalArgumentException("As per xAPI spec: objectType for group must be group")
    }

    /**
     * True if this is an anonymous group
     */
    val isAnonymous: Boolean
        get() = name == null && mbox == null && mbox_sha1sum == null &&
                openid == null && account == null

    val isIdentified: Boolean = !isAnonymous

}


/**
 * As per the Xapi Spec: the objectType on a group is required, and it must be "group".
 * Serialization by default will not encode default property values, so this leads to incorrect
 * output for XapiGroup. An Xapi Actor property which has no defined objectType is considered to be
 * an Agent.
 */
object XapiGroupTransformingSerializer: JsonTransformingSerializer<XapiGroup>(
    XapiGroup.generatedSerializer()
) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        return buildJsonObject {
            putAll(element as JsonObject)
            put("objectType", JsonPrimitive(XapiObjectType.Group.value))
        }
    }
}
