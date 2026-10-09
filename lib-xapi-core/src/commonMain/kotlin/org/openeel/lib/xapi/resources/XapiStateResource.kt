package world.respect.lib.xapi.resources

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.util.StringValues
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.getUuidOrNull
import world.respect.lib.xapi.model.XapiAgent
import world.respect.libutil.ext.appendIfNotNull
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Xapi State Resource
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#23-state-resource
 */
interface XapiStateResource: XapiDocumentResource<XapiStateResource.MultiDocParams, XapiStateResource.SingleDocumentParams> {

    data class MultiDocParams(
        val activityId: String,
        val agent: XapiAgent,
        val registration: Uuid? = null,
        val since: Instant? = null,
    ) {


        fun toParameters(json: Json): Parameters {
            return ParametersBuilder().also { parameters ->
                parameters.append("activityId", activityId)
                parameters.append("agent", json.encodeToString(XapiAgent.serializer(), agent))
                parameters.appendIfNotNull("registration", registration)
                parameters.appendIfNotNull("since", since?.toString())
            }.build()
        }

        companion object {
            fun fromParameters(params: StringValues, json: Json): MultiDocParams {
                return MultiDocParams(
                    activityId = params["activityId"] ?: throw XapiException(400, "activityId is required"),
                    agent = params["agent"]?.let { json.decodeFromString(XapiAgent.serializer(), it) }
                        ?: throw XapiException(400, "agent is required"),
                    registration = params.getUuidOrNull("registration"),
                    since = params["since"]?.let { Instant.parse(it) },
                )
            }
        }
    }

    data class SingleDocumentParams(
        val activityId: String,
        val agent: XapiAgent,
        val registration: Uuid? = null,
        val stateId: String,
    ) : ISingleDocumentParams<MultiDocParams> {

        override val idString: String = stateId

        override fun toMultiDocParams(since: Instant?): MultiDocParams {
            return MultiDocParams(
                activityId = activityId,
                agent = agent,
                registration = registration,
                since = since,
            )
        }

        override fun matches(multiDocParams: MultiDocParams): Boolean {
            return multiDocParams.activityId == activityId &&
                    multiDocParams.agent == agent &&
                    multiDocParams.registration == registration
        }

        fun toParameters(json: Json): Parameters {
            return ParametersBuilder().also { parameters ->
                parameters.append("activityId", activityId)
                parameters.append("agent", json.encodeToString(XapiAgent.serializer(), agent))
                parameters.appendIfNotNull("registration", registration)
                parameters.append("stateId", stateId)
            }.build()
        }

        companion object {
            fun fromParameters(params: StringValues, json: Json): SingleDocumentParams {
                return SingleDocumentParams(
                    activityId = params["activityId"] ?: throw XapiException(400, "activityId is required"),
                    agent = params["agent"]?.let { json.decodeFromString(XapiAgent.serializer(), it) }
                        ?: throw XapiException(400, "agent is required"),
                    registration = params.getUuidOrNull("registration"),
                    stateId = params["stateId"] ?: throw XapiException(400, "stateId is required"),
                )
            }
        }
    }

    companion object {

        const val ENDPOINT_NAME = "state"

    }

}