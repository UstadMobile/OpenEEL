package org.openeel.lib.xapi.permissionmanager

import org.openeel.lib.xapi.permissionmanager.policy.XapiPolicy
import org.openeel.lib.xapi.resources.XapiActivitiesResource
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiAgentsResource
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.lib.xapi.resources.XapiStatementsResource

/**
 * Permission management wrapper for an [XapiResource].
 */
class XapiResourcePermissionManager(
    private val resource: XapiResource,
    private val policy: XapiPolicy,
) : XapiResource {

    override val statements: XapiStatementsResource by lazy {
        XapiStatementResourcePermissionManager(
            resource.statements, policy.statements
        )
    }

    override val agents: XapiAgentsResource by lazy {
        resource.agents
    }

    override val activities: XapiActivitiesResource by lazy {
        resource.activities
    }

    override val state: XapiStateResource by lazy {
        XapiStateResourcePermissionManager(resource.state, policy.state)
    }

    override val activityProfile: XapiActivityProfileResource by lazy {
        XapiActivityProfileResourcePermissionManager(
            resource.activityProfile, policy.activityProfile
        )
    }
    override val agentProfile: XapiAgentProfileResource by lazy {
        XapiAgentProfileResourcePermissionManager(
            resource.agentProfile, policy.agentProfile
        )
    }

    override fun close() {
        resource.close()
    }

}