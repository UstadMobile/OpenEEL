package world.respect.shared.ext

import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ext.hasRel

fun List<ReadiumLink>.alternateLanguageLinks(): List<ReadiumLink> {
    return this.filter {
        it.hasRel("alternate") && !it.language.isNullOrEmpty()
    }
}
