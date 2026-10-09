package world.respect.lib.xapi.resources.local

import world.respect.lib.xapi.resources.XapiResource

interface XapiResourceLocal: XapiResource {

    override val statements: XapiStatementsResourceLocal

    override val agents: XapiAgentsResourceLocal

    override val activities: XapiActivitiesResourceLocal

    override val activityProfile: XapiActivityProfileResourceLocal

    override val agentProfile: XapiAgentProfileResourceLocal

    override val state: XapiStateResourceLocal

}