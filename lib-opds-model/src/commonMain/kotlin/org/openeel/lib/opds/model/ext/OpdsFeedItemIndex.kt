package world.respect.lib.opds.model.ext


/**
 * Index for an item (could be used for publications or navigation links) in an OpdsFeed. There
 * is no guarantee in the Opds spec that each item will have an identifier, or that any given
 * identifier will be unique.
 *
 * @param groupIndex index of the group the selected item is in, or -1 if it is not in a group
 * @param index index of the item within the group if in a group, otherwise its index within the
 *        publications/navigation list.
 */
data class OpdsFeedItemIndex(
    val groupIndex: Int,
    val index: Int,
)
