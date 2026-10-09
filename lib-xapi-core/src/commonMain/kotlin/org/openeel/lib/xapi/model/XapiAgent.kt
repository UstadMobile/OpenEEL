package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class XapiAgent(
    override val name: String? = null,
    override val mbox: String? = null,
    override val mbox_sha1sum: String? = null,
    override val openid: String? = null,
    override val objectType: XapiObjectType? = null,
    override val account: XapiAccount? = null,
): XapiActor, XapiStatementObject
