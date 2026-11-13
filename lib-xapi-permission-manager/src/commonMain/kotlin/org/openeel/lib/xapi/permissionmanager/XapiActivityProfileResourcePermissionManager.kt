package org.openeel.lib.xapi.permissionmanager

import org.openeel.lib.xapi.permissionmanager.policy.XapiActivityProfilePolicy
import org.openeel.lib.xapi.resources.XapiActivityProfileResource

class XapiActivityProfileResourcePermissionManager(
    resource: XapiActivityProfileResource,
    policy: XapiActivityProfilePolicy
) : AbstractDocumentResourcePermissionManager<
        XapiActivityProfileResource.MultiDocParams, XapiActivityProfileResource.SingleDocumentParams
>(resource, policy), XapiActivityProfileResource

