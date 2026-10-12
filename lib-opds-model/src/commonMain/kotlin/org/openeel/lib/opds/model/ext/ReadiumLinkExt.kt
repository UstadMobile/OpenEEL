package org.openeel.lib.opds.model.ext

import org.openeel.lib.opds.model.ReadiumLink


fun ReadiumLink.hasRel(relationship: String): Boolean {
    return this.rel?.let { rels -> relationship in rels } ?: false
}

fun List<ReadiumLink>.filterByHasRel(
    rel: String,
): List<ReadiumLink> {
    return filter { it.hasRel(rel) }
}
