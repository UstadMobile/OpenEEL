package world.respect.shared.domain.launchapp.getxapilaunchparams

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.lib.xapi.model.XapiActor
import kotlin.uuid.Uuid

/**
 *
 */
@Serializable
data class XapiLaunchParams(
    val activityId: String,
    val endpoint: Url,
    val actor: XapiActor,
    val auth: String,
    val registration: Uuid?,
)