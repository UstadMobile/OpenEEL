package org.openeel.shared.util.ext

import org.openeel.shared.navigation.OpdsFeedEdit

/**
 * True if the destination is for a new feed or a remix of an existing feed,
 * in which case navigation will need to follow the new entity pattern.
 */
val OpdsFeedEdit.isNewFeedOrNewRemix: Boolean
    get() = url == null || isCopy
