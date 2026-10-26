package org.openeel.lib.xapi.resources

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.util.StringValues
import kotlinx.serialization.json.Json
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.libutil.ext.appendIfNotNull
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
    ) : ISingleDocumentParams {

        override val idString: String
            get() = profileId

        override fun toParameters(json: Json): Parameters {
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

    class ParamsAdapter : XapiDocumentResourceParamsAdapter<MultiDocParams, SingleDocumentParams> {
        override fun singleDocParamsToMutliDocParams(
            params: SingleDocumentParams,
            since: Instant?
        ): MultiDocParams {
            return MultiDocParams(
                activityId = params.activityId,
                since = since
            )
        }

        override fun singleDocParamsMatchMultiDocParams(
            singleDocParams: SingleDocumentParams,
            multiDocParams: MultiDocParams
        ): Boolean {
            return multiDocParams.activityId == singleDocParams.activityId
        }

        override fun singleDocParamsFromParameters(params: Parameters, json: Json): SingleDocumentParams {
            return SingleDocumentParams.fromParameters(params)
        }
    }

    companion object {

        const val ENDPOINT_NAME = "profile"

    }

}