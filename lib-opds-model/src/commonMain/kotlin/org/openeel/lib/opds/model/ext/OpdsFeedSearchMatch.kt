package org.openeel.lib.opds.model.ext

import kotlinx.serialization.Serializable

/** Group position in the feed, or -1 for an item at the top level. */
/** Original position within the feed or group list. */
/** Distinguishes publications from navigation links, which can share the same indexes. */

@Serializable
data class OpdsFeedSearchMatch(
    val groupIndex: Int,
    val index: Int,
    val isPublication: Boolean,
)