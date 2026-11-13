package org.openeel.lib.xapi.permissionmanager

import org.openeel.lib.xapi.permissionmanager.policy.XapiAgentProfileResourcePolicy
import org.openeel.lib.xapi.resources.XapiAgentProfileResource

class XapiAgentProfileResourcePermissionManager(
    resource: XapiAgentProfileResource,
    policy: XapiAgentProfileResourcePolicy,
): AbstractDocumentResourcePermissionManager<
    XapiAgentProfileResource.MultiDocParams, XapiAgentProfileResource.SingleDocumentParams
>(resource, policy), XapiAgentProfileResource
