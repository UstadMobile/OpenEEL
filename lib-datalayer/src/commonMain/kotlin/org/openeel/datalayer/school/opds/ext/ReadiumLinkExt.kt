package org.openeel.datalayer.school.opds.ext

import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ext.hasRel

/**
 * Find icons for a [ReadiumLink]. This isn't really defined so well externally (e.g. publications
 * have a dedicated images collection), so here we are looking for a link in the alternate sub
 * element with the rel "icon".
 */
fun ReadiumLink.findIcons(): List<ReadiumLink> {
    return this.alternate.orEmpty().filter { it.hasRel("icon") }
}
