package org.openeel.lib.opds.model.ext

import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.util.NavigationItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedGroupItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedItemIndex
import org.openeel.lib.opds.model.util.PublicationItemItemIndex

fun OpdsFeed.feedIconLinkOrNull(): ReadiumLink? {
    return links.filterByHasRel("icon").firstOrNull()
}

fun OpdsFeed.allPublications(): List<Publication> {
    return publications.orEmpty() + groups.orEmpty().flatMap {
        it.publications.orEmpty()
    }
}

/**
 * Determine whether the given index, as part of the receiver feed, is part of a navigation
 * collection (either in the top level navigation collection or in a group).
 *
 * @receiver The [OpdsFeed] the index is for
 * @param index [OpdsFeedItemIndex] to lookup
 * @return true if the given index is part of a navigation collection, false otherwise.
 */
fun OpdsFeed.indexIsInNavigationCollection(index: OpdsFeedItemIndex) : Boolean {
    return when(index) {
        is NavigationItemItemIndex -> true

        is OpdsFeedGroupItemItemIndex -> {
            groups?.getOrNull(index.groupIndex)?.navigation?.getOrNull(index.index) != null
        }

        else -> false
    }
}

fun OpdsFeed.getPublicationByIndex(index: OpdsFeedItemIndex): Publication {
    return when(index) {
        is OpdsFeedGroupItemItemIndex -> {
            groups.orEmpty()[index.groupIndex].publications.orEmpty()[index.index]
        }

        is PublicationItemItemIndex -> {
            publications.orEmpty()[index.index]
        }

        else -> {
            throw IllegalArgumentException("getPublicationByIndex: not a publication or group index")
        }
    }
}

fun OpdsFeed.getPublicationsByIndexes(
    indexes: Collection<OpdsFeedItemIndex>
) : List<Publication>{
    return publications?.filterIndexed { index, _ ->
        PublicationItemItemIndex(index) in indexes
    }.orEmpty() + groups?.flatMapIndexed { groupIndex: Int, group ->
        group.publications?.filterIndexed { index, _ ->
            OpdsFeedGroupItemItemIndex(groupIndex, index) in indexes
        }.orEmpty()
    }.orEmpty()
}

fun OpdsFeed.getNavigationLinkByIndex(
    index: OpdsFeedItemIndex
): ReadiumLink {
    return if(index is OpdsFeedGroupItemItemIndex){
        this.groups?.get(index.groupIndex)?.navigation?.get(index.index)
            ?: throw IllegalArgumentException("No navigation link at $index")
    }else {
        this.navigation?.get(index.index) ?: throw IllegalArgumentException("No navigation at $index")
    }
}

fun OpdsFeed.getNavigationLinksByIndexes(
    indexes: Collection<OpdsFeedItemIndex>
): List<ReadiumLink> {
    return navigation?.filterIndexed { index, _ ->
        OpdsFeedGroupItemItemIndex(-1, index) in indexes
    }.orEmpty() + groups?.flatMapIndexed { groupIndex: Int, group ->
        group.navigation?.filterIndexed { index, _ ->
            OpdsFeedGroupItemItemIndex(groupIndex, index) in indexes
        }.orEmpty()
    }.orEmpty()
}
