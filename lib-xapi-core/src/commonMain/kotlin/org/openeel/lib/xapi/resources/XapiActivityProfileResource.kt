package world.respect.lib.xapi.resources

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.util.StringValues
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.libutil.ext.appendIfNotNull
import kotlin.time.Instant

/**
 * Xapi Activity Profile resource : implements the Document Resource for Activity Profiles as per:
 *
 *  https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource
 *
 */
interface XapiActivityProfileResource : XapiDocumentResource<XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams> {

    data class MultiDocParams(
        val activityId: String,
        val since: Instant? = null,
    ) {
        fun toParameters(): StringValues {
            return ParametersBuilder().also { parameters ->
                parameters.append("activityId", activityId)
                parameters.appendIfNotNull("since", since?.toString())
            }.build()
        }

        companion object {
            fun fromParameters(params: StringValues): MultiDocParams {
                return MultiDocParams(
                    activityId = params["activityId"] ?: throw XapiException(400, "activityId is required"),
                    since = params["since"]?.let { Instant.parse(it) },
                )
            }
        }
    }

    data class SingleDocumentParams(
        val activityId: String,
        val profileId: String,
    ) : ISingleDocumentParams<MultiDocParams> {

        override val idString: String
            get() = profileId

        override fun matches(multiDocParams: MultiDocParams): Boolean {
            return multiDocParams.activityId == activityId
        }

        override fun toMultiDocParams(
            since: Instant?,
        ): MultiDocParams {
            return MultiDocParams(activityId = activityId, since = since)
        }

        fun toParameters(): StringValues {
            return ParametersBuilder().also { parameters ->
                parameters.append("activityId", activityId)
                parameters.append("profileId", profileId)
            }.build()
        }

        companion object {
            fun fromParameters(params: StringValues): SingleDocumentParams {
                return SingleDocumentParams(
                    activityId = params["activityId"] ?: throw XapiException(400, "activityId is required"),
                    profileId = params["profileId"] ?: throw XapiException(400, "profileId is required"),
                )
            }
        }
    }

    companion object {

        const val ENDPOINT_NAME = "profile"

        const val KEY_SHARED_DEVICE_SELF_SELECT = "shared-device-self-select"

        const val KEY_SHARED_DEVICE_PIN = "shared-device-pin"

    }

}