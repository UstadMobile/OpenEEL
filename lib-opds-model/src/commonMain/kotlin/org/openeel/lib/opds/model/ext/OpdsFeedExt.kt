package org.openeel.lib.opds.model.ext

import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.util.OpdsFeedGroupItemItemIndex

fun OpdsFeed.feedIconLinkOrNull(): ReadiumLink? {
    return links.filterByHasRel("icon").firstOrNull()
}

fun OpdsFeed.allPublications(): List<Publication> {
    return publications.orEmpty() + groups.orEmpty().flatMap {
        it.publications.orEmpty()
    }
}

fun OpdsFeed.getPublicationByIndex(opdsFeedItemIndex: OpdsFeedGroupItemItemIndex): Publication {
    return if(opdsFeedItemIndex.groupIndex >= 0) {
        this.groups?.get(opdsFeedItemIndex.groupIndex)?.publications?.get(opdsFeedItemIndex.index)
            ?: throw IllegalArgumentException(
                "No publication at groupIndex=:${opdsFeedItemIndex.groupIndex}/${opdsFeedItemIndex.index}"
            )
    }else {
        this.publications?.get(opdsFeedItemIndex.index)
            ?: throw IllegalArgumentException("No publication at index ${opdsFeedItemIndex.index}")
    }
}

fun OpdsFeed.getPublicationsByIndexes(
    indexes: Collection<OpdsFeedGroupItemItemIndex>
) : List<Publication>{
    return publications?.filterIndexed { index, _ ->
        OpdsFeedGroupItemItemIndex(-1, index) in indexes
    }.orEmpty() + groups?.flatMapIndexed { groupIndex: Int, group ->
        group.publications?.filterIndexed { index, _ ->
            OpdsFeedGroupItemItemIndex(groupIndex, index) in indexes
        }.orEmpty()
    }.orEmpty()
}

fun OpdsFeed.getNavigationLinkByIndex(opdsFeedItemIndex: OpdsFeedGroupItemItemIndex): ReadiumLink {
    return if(opdsFeedItemIndex.groupIndex >= 0){
        this.groups?.get(opdsFeedItemIndex.groupIndex)?.navigation?.get(opdsFeedItemIndex.index)
            ?: throw IllegalArgumentException("No navigation link at $opdsFeedItemIndex")
    }else {
        this.navigation?.get(opdsFeedItemIndex.index) ?: throw IllegalArgumentException("No navigation at $opdsFeedItemIndex")
    }
}

fun OpdsFeed.getNavigationLinksByIndexes(
    indexes: Collection<OpdsFeedGroupItemItemIndex>
): List<ReadiumLink> {
    return navigation?.filterIndexed { index, _ ->
        OpdsFeedGroupItemItemIndex(-1, index) in indexes
    }.orEmpty() + groups?.flatMapIndexed { groupIndex: Int, group ->
        group.navigation?.filterIndexed { index, _ ->
            OpdsFeedGroupItemItemIndex(groupIndex, index) in indexes
        }.orEmpty()
    }.orEmpty()
}
