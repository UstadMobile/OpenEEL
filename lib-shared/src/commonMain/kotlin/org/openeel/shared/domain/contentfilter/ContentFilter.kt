package org.openeel.shared.domain.contentfilter

import org.jetbrains.compose.resources.StringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.book
import org.openeel.shared.generated.resources.collection
import org.openeel.shared.generated.resources.game
import org.openeel.shared.generated.resources.video

/**
 * Pre-defined content types that can be used to filter OPDS content.
 *
 * @property schemaOrgId the schema.org id to match against Publication metadata @type, or null for
 *           COLLECTION, which matches navigation links (e.g. links to other OpdsFeeds).
 */
enum class ContentFilterType(
    val label: StringResource,
    val schemaOrgId: String?,
) {
    COLLECTION(Res.string.collection, null),
    GAME(Res.string.game, "http://schema.org/Game"),
    BOOK(Res.string.book, "http://schema.org/Book"),
    VIDEO(Res.string.video, "http://schema.org/VideoObject");
}

/**
 * An option that can be selected in a content filter.
 */
sealed class ContentFilterOption {

    /** Publications where metadata.language contains [langCode]. */
    data class Language(val langCode: String) : ContentFilterOption()

    /** Publications where metadata.subject contains a subject with the name [subject]. */
    data class Subject(val subject: String) : ContentFilterOption()

    /** Publications of the given type, or navigation links for COLLECTION. */
    data class Type(val type: ContentFilterType) : ContentFilterOption()

}
