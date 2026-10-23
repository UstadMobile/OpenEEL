package org.openeel.lib.xapi.resources

import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/**
 * Interface implemented by the SingleDocParams on xAPI document resources.
 */
interface ISingleDocumentParams<MultiDocParams: Any> {

    /**
     * Each of the xAPI use a string id: profileId for the activity profile resource
     * and agent profile resource, and stateId for the state resource. This is the string id
     * returned by getMultipleDocuments
     */
    val idString: String

    /**
     * Convert this parameters object into KTOR Parameters that can be used with a HTTP request.
     */
    fun toParameters(json: Json): Parameters

    fun matches(multiDocParams: MultiDocParams): Boolean

    /**
     * Each of the xAPI document resources (Activity Profile, State, and Agent profile)
     * MultiDocParams require all the parameters of the SingleDocParams except string id (
     * profileId or stateId as per [idString].
     *
     * All MultiDocParams always include the since parameter.
     *
     * @param since the since parameter to use for the MutliDocParams to be returned
     * @return MultiDocParams corresponding to these SingleDocumentParams
     */
    fun toMultiDocParams(
        since: Instant? = null,
    ): MultiDocParams

}