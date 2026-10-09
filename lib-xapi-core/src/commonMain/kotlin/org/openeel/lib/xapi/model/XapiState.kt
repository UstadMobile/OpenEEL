package world.respect.lib.xapi.model

class XapiState(
    val stateId: String?,
    val agent: XapiActor?,
    val activityId: String?,
    val content: HashMap<String, Any>?,
    val registration: String?
)
