package org.openeel.shared.ext

import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ext.hasRel

fun List<ReadiumLink>.alternateLanguageLinks(): List<ReadiumLink> {
    return this.filter {
        it.hasRel("alternate") && !it.language.isNullOrEmpty()
    }
}
