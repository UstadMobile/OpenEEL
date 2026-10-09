package world.respect.lib.xapi.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import world.respect.lib.xapi.exceptions.XapiException

/**
 * An XapiActor can be an Agent or Group as per the spec
 */
@Serializable(with = XapiActorSerializer::class)
sealed interface XapiActor {
    val name: String?
    val mbox: String?
    val mbox_sha1sum: String?
    val openid: String?
    val objectType: XapiObjectType?
    val account: XapiAccount?
}

object XapiActorSerializer: JsonContentPolymorphicSerializer<XapiActor>(XapiActor::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<XapiActor> {
        val objectType = element.jsonObject["objectType"]
            ?.jsonPrimitive?.contentOrNull?.let { XapiObjectType.valueOf(it) }
            ?: XapiObjectType.Agent

        return when (objectType) {
            XapiObjectType.Agent -> XapiAgent.serializer()
            XapiObjectType.Group -> XapiGroup.serializer()
            else -> throw XapiException(400, "Invalid object type for actor: must be Agent or Group")
        }
    }
}
