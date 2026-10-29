package org.openeel.shared.ext

import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ext.hasRel
import org.openeel.shared.util.ext.resolveAll
import org.openeel.shared.viewmodel.catalog.OpdsFeedSelection

fun List<ReadiumLink>.alternateLanguageLinks(): List<ReadiumLink> {
    return this.filter {
        it.hasRel("alternate") && !it.language.isNullOrEmpty()
    }
}

fun List<ReadiumLink>.addSelection(selection: OpdsFeedSelection) : List<ReadiumLink>{
    return selection.selectedFeeds.resolveAll(selection.url) + this
}

fun ReadiumLink.findIcons(): List<ReadiumLink> {
    return this.alternate.orEmpty().filter { it.hasRel("icon") }
}
