package org.openeel.lib.xapi.permissionmanager.policy

import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiStateResource


typealias XapiActivityProfilePolicy = XapiDocumentResourcePolicy<
    XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams
>

typealias XapiStateResourcePolicy = XapiDocumentResourcePolicy<
    XapiStateResource.MultiDocParams, XapiStateResource.SingleDocumentParams
>

typealias XapiAgentProfileResourcePolicy = XapiDocumentResourcePolicy<
    XapiAgentProfileResource.MultiDocParams, XapiAgentProfileResource.SingleDocumentParams
>

/**
 * An XapiPolicy has properties for the [XapiStatementsResourcePolicy] and an
 * [XapiDocumentResourcePolicy] for each of the document profile resources.
 */
interface XapiPolicy {

    val statements: XapiStatementsResourcePolicy

    val activityProfile: XapiActivityProfilePolicy

    val state: XapiStateResourcePolicy

    val agentProfile: XapiAgentProfileResourcePolicy

}