package world.respect.lib.xapi.resources

interface XapiResource {

    val statements: XapiStatementsResource

    val agents: XapiAgentsResource

    val activities: XapiActivitiesResource

    val state: XapiStateResource

    val activityProfile: XapiActivityProfileResource

    val agentProfile: XapiAgentProfileResource

    fun close()


}