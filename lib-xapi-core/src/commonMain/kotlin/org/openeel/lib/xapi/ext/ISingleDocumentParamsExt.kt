package org.openeel.lib.xapi.ext

import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiStateResource

/**
 * Copy these parameters to have the same parameters, but with the given idString. See
 * [ISingleDocumentParams.idString] for doc on which parameter is the idString.
 */
@Suppress("UNCHECKED_CAST")
fun <T: ISingleDocumentParams> T.copyWithIdString(
    idString: String
): T {
    return when(this) {
        is XapiActivityProfileResource.SingleDocumentParams -> copy(profileId = idString)
        is XapiAgentProfileResource.SingleDocumentParams -> copy(profileId = idString)
        is XapiStateResource.SingleDocumentParams -> copy(stateId = idString)
    } as T
}
