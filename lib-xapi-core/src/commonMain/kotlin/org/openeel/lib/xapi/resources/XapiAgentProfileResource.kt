package world.respect.lib.xapi.resources

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.util.StringValues
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.requireIfi
import world.respect.lib.xapi.model.XapiAgent
import world.respect.libutil.ext.appendIfNotNull
import kotlin.time.Instant

/**
 * Agents Profile Resource
 *
 * See
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#26-agent-profile-resource
 */
interface XapiAgentProfileResource: XapiDocumentResource<XapiAgentProfileResource.MultiDocParams, XapiAgentProfileResource.SingleDocumentParams> {

    data class MultiDocParams(
        val agent: XapiAgent,
        val since: Instant? = null,
    ) {
        fun toParameters(json: Json): Parameters {
            return ParametersBuilder().also { parameters ->
                parameters.append("agent", json.encodeToString(XapiAgent.serializer(), agent))
                parameters.appendIfNotNull("since", since?.toString())
            }.build()
        }

        companion object {
            fun fromParameters(params: StringValues, json: Json): MultiDocParams {
                return MultiDocParams(
                    agent = params["agent"]?.let { json.decodeFromString(XapiAgent.serializer(), it) }
                        ?: throw XapiException(400, "agent is required"),
                    since = params["since"]?.let { Instant.parse(it) },
                )
            }
        }
    }

    data class SingleDocumentParams(
        val agent: XapiAgent,
        val profileId: String,
    ): ISingleDocumentParams<MultiDocParams> {
        override val idString: String = profileId

        override fun matches(multiDocParams: MultiDocParams): Boolean {
            return multiDocParams.agent.requireIfi() == this.agent.requireIfi()
        }

        override fun toMultiDocParams(since: Instant?): MultiDocParams {
            return MultiDocParams(agent, since)
        }

        fun toParameters(json: Json): Parameters {
            return ParametersBuilder().also { parameters ->
                parameters.append("agent", json.encodeToString(XapiAgent.serializer(), agent))
                parameters.append("profileId", profileId)
            }.build()
        }

        companion object {
            fun fromParameters(params: StringValues, json: Json): SingleDocumentParams {
                return SingleDocumentParams(
                    agent = params["agent"]?.let { json.decodeFromString(XapiAgent.serializer(), it) }
                        ?: throw XapiException(400, "agent is required"),
                    profileId = params["profileId"] ?: throw XapiException(400, "profileId is required"),
                )
            }
        }
    }

    companion object {
        const val ENDPOINT_NAME = "profile"
    }

}