package org.openeel.datalayer.school.model.composites

import kotlinx.serialization.Serializable

/**
 * An item (publication or navigation link to a feed) found when searching cached OPDS data.
 *
 * @property title the title of the publication or navigation link
 * @property isPublication true for a publication, false for a navigation link (collection)
 * @property url the absolute URL of a publication that was loaded by itself (e.g. not as part of
 *           a feed), otherwise null.
 * @property href the publication self link href or navigation link href. May be relative to
 *           [feedUrl].
 * @property feedUrl the URL of the feed in which the item was found, if any.
 * @property feedTitle the title of the feed in which the item was found, if any.
 * @property identifier the publication metadata identifier, if any.
 * @property iconHref the publication's first image or the navigation link's icon (alternate link
 *           with rel icon), if any. May be relative to [url] or [feedUrl].
 */
@Serializable
data class OpdsSearchResult(
    val title: String,
    val isPublication: Boolean,
    val url: String? = null,
    val href: String? = null,
    val feedUrl: String? = null,
    val feedTitle: String? = null,
    val identifier: String? = null,
    val iconHref: String? = null,
)
