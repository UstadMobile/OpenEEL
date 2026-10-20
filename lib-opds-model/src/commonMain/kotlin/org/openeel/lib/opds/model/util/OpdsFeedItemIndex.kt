package org.openeel.lib.opds.model.util


/**
 * Index for an item (which could be a [org.openeel.lib.opds.model.Publication] or
 * [org.openeel.lib.opds.model.ReadiumLink] within an OpdsFeed. The item could be contained in the
 * top level publications or navigation collections, or in a group.
 *
 * This is used when handling reordering, selection, clicks, etc.
 */
sealed class OpdsFeedItemIndex(val index: Int)

/**
 * Index for a [org.openeel.lib.opds.model.ReadiumLink] that is contained within the top level
 * navigation collection of an [org.openeel.lib.opds.model.OpdsFeed]
 */
class NavigationItemItemIndex(index: Int): OpdsFeedItemIndex(index)

/**
 * Index for a [org.openeel.lib.opds.model.Publication] that is contained within the top level
 * publication collection of an [org.openeel.lib.opds.model.OpdsFeed]
 */
class PublicationItemItemIndex(index: Int): OpdsFeedItemIndex(index)

/**
 * Index for an item that is contained within an [org.openeel.lib.opds.model.OpdsGroup]. It could
 * be a Navigation Item ([org.openeel.lib.opds.model.ReadiumLink]) or
 * [org.openeel.lib.opds.model.Publication].
 *
 * @param groupIndex index of the group the selected item is in
 * @param index index of the item within the group if in a group
 */
class OpdsFeedGroupItemItemIndex(
    val groupIndex: Int,
    index: Int,
) : OpdsFeedItemIndex(index) {

    companion object {

        const val NO_GROUP = -1

    }

}
