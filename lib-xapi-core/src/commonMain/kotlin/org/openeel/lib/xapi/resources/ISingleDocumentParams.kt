package org.openeel.lib.xapi.resources

import io.ktor.http.Parameters
import kotlinx.serialization.json.Json

/**
 * Interface implemented by the SingleDocParams on xAPI document resources.
 */
sealed interface ISingleDocumentParams {

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

}