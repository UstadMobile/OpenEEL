package world.respect.shared.viewmodel.catalog

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.lib.opds.model.ReadiumLink

@Serializable
data class OpdsFeedSelection(
    val url: Url,
    val selectedFeeds: List<ReadiumLink>,
)