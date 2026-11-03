package org.openeel.shared.util.ext

import io.ktor.http.Url
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.util.NavigationItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedGroupItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedItemIndex
import org.openeel.lib.opds.model.util.PublicationItemItemIndex
import org.openeel.shared.ext.addSelection
import org.openeel.shared.viewmodel.catalog.OpdsFeedSelection
import org.openeel.shared.viewmodel.catalog.PublicationsSelection
import org.openeel.shared.viewmodel.catalog.opdsfeededit.OpdsGroupType
import kotlin.collections.orEmpty
import kotlin.collections.toMutableList

/**
 * Resolves all relative URLs (navigation, images, groups, alternates)
 * inside an OpdsFeed against the given base URL.
 */
fun OpdsFeed.resolve(baseUrl: Url): OpdsFeed {
    return this.copy(
        navigation = navigation?.resolveAll(baseUrl),
        publications = publications?.map { it.resolve(baseUrl) },
        groups = groups?.map { group ->
            group.copy(
                publications = group.publications?.map { it.resolve(baseUrl) },
                navigation = group.navigation?.resolveAll(baseUrl),
            )
        },
    )
}

/**
 * Add the [OpdsFeedSelection] as per parameter to the receiver [OpdsFeed]
 *
 * @param selection The [OpdsFeedSelection] to add
 * @param toGroupIndex the group index to add the selection to, or OpdsFeedItemIndex.NO_GROUP to
 *        put it in the top level navigation collection.
 * @receiver the [OpdsFeed] the selection is to be added to
 * @return the receiver [OpdsFeed] with the selection added
 */
fun OpdsFeed.addFeedSelection(
    selection: OpdsFeedSelection,
    toGroupIndex: Int,
): OpdsFeed {
    return if(toGroupIndex == OpdsFeedItemIndex.NO_GROUP) {
        copy(navigation = navigation.orEmpty().addSelection(selection))
    }else {
        copy(
            groups = groups.orEmpty().toMutableList().also { groupList ->
                val groupToAddTo = groupList[toGroupIndex]
                groupList[toGroupIndex] = groupToAddTo.copy(
                    navigation = groupToAddTo.navigation.orEmpty().addSelection(selection)
                )
            }.toList()
        )
    }
}


/**
 * Add the [PublicationsSelection] as per parameter to the receiver [OpdsFeed]
 * @param selection The [PublicationsSelection] to add
 * @param toGroupIndex the group index to add the selection to, or OpdsFeedItemIndex.NO_GROUP to
 *        put it in the top level publications collection.
 * @receiver the [OpdsFeed] the selection is to be added to
 * @return the receiver [OpdsFeed] with the selection added
 */
fun OpdsFeed.addPublicationSelection(
    selection: PublicationsSelection,
    toGroupIndex: Int
): OpdsFeed {
    return if(toGroupIndex == OpdsFeedItemIndex.NO_GROUP) {
        copy(publications = publications.orEmpty().resolveAndAddSelection(selection))
    }else {
        copy(
            groups = groups.orEmpty().toMutableList().also { groupList ->
                groupList[toGroupIndex] = groupList[toGroupIndex].copy(
                    publications = groupList[toGroupIndex].publications
                        .orEmpty().resolveAndAddSelection(selection),
                )
            }
        )
    }
}

fun OpdsFeed.removeItemAt(
    itemIndex: OpdsFeedItemIndex
): OpdsFeed {
    return when (itemIndex) {
        is OpdsFeedGroupItemItemIndex -> {
            copy(
                groups = groups.orEmpty().toMutableList().also { groups ->
                    val groupType = groups[itemIndex.groupIndex].groupType
                    if (groupType == OpdsGroupType.NAVIGATION) {
                        groups[itemIndex.groupIndex] = groups[itemIndex.groupIndex].copy(
                            navigation = groups[itemIndex.groupIndex].navigation.orEmpty()
                                .toMutableList().also { it.removeAt(itemIndex.index) }
                        )
                    } else {
                        groups[itemIndex.groupIndex] = groups[itemIndex.groupIndex].copy(
                            publications = groups[itemIndex.groupIndex].publications.orEmpty()
                                .toMutableList().also { it.removeAt(itemIndex.index) }
                        )
                    }
                }.toList()
            )
        }

        is NavigationItemItemIndex -> {
            copy(
                navigation = navigation.orEmpty().toMutableList().also {
                    it.removeAt(itemIndex.index)
                }.toList()
            )
        }

        is PublicationItemItemIndex -> {
            copy(
                publications = publications.orEmpty().toMutableList().also {
                    it.removeAt(itemIndex.index)
                }
            )
        }
    }
}
