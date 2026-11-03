package org.openeel.lib.xapi.permissionmanager

import org.openeel.lib.xapi.permissionmanager.policy.XapiStateResourcePolicy
import org.openeel.lib.xapi.resources.XapiStateResource

class XapiStateResourcePermissionManager(
    resource: XapiStateResource,
    policy: XapiStateResourcePolicy
) : AbstractDocumentResourcePermissionManager<XapiStateResource.MultiDocParams, XapiStateResource.SingleDocumentParams>(
    resource, policy
), XapiStateResource
