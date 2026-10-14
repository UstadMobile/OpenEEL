package org.openeel.shared.domain.contentfilter

import org.jetbrains.compose.resources.StringResource
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ReadiumSubject
import org.openeel.lib.opds.model.ext.OpdsFeedItemIndex
import org.openeel.lib.opds.model.ext.OpdsFeedSearchMatch
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.book
import org.openeel.shared.generated.resources.collection
import org.openeel.shared.generated.resources.game
import org.openeel.shared.generated.resources.video

/**
 * Pre-defined content types that can be used to filter OPDS feed items.
 *
 * @property schemaOrgId the schema.org id to match against Publication metadata @type, or null for
 *           COLLECTION, which matches anything that is an OpdsFeed (e.g. navigation links).
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

    /** Matches publications where metadata.language contains [langCode]. */
    data class Language(val langCode: String) : ContentFilterOption()

    /** Matches publications where metadata.subject contains [subject]. */
    data class Subject(val subject: ReadiumSubject) : ContentFilterOption()

    /** Matches publications of the given type, or navigation links for COLLECTION. */
    data class Type(val type: ContentFilterType) : ContentFilterOption()

}

/**
 * The options available for the data driven filters (language and subject).
 *
 * @property languages distinct language codes from publication metadata.language
 * @property subjects distinct subjects from publication metadata.subject
 */
data class ContentFilterOptions(
    val languages: List<String> = emptyList(),
    val subjects: List<ReadiumSubject> = emptyList(),
) {

    companion object {

        fun fromPublications(publications: List<Publication>): ContentFilterOptions {
            return ContentFilterOptions(
                languages = publications.flatMap { it.metadata.language.orEmpty() }.distinct(),
                subjects = publications.flatMap { it.metadata.subject.orEmpty() }.distinct(),
            )
        }

    }
}

/**
 * The filter options selected by the user. Options within the same filter are combined using OR,
 * different filters are combined using AND.
 *
 * @property selected the options selected by the user
 */
data class ContentFilterSelection(
    val selected: Set<ContentFilterOption> = emptySet(),
) {

    private val languages = selected.filterIsInstance<ContentFilterOption.Language>()
        .map { it.langCode }.toSet()

    private val subjects = selected.filterIsInstance<ContentFilterOption.Subject>()
        .map { it.subject }.toSet()

    private val types = selected.filterIsInstance<ContentFilterOption.Type>()
        .map { it.type }.toSet()

    val isEmpty: Boolean
        get() = selected.isEmpty()

    fun isSelected(option: ContentFilterOption): Boolean = option in selected

    fun toggle(option: ContentFilterOption): ContentFilterSelection = copy(
        selected = if (option in selected) selected - option else selected + option
    )

    fun matchesPublication(publication: Publication): Boolean {
        val metadata = publication.metadata

        val languageMatches = languages.isEmpty() ||
            metadata.language.orEmpty().any { it in languages }

        val subjectMatches = subjects.isEmpty() ||
            metadata.subject.orEmpty().any { it in subjects }

        val publicationType = metadata.type?.toString()
        // COLLECTION has no schema.org id, so a publication without @type must not match it
        val typeMatches = types.isEmpty() ||
            (publicationType != null && types.any { it.schemaOrgId == publicationType })

        return languageMatches && subjectMatches && typeMatches
    }

    /**
     * Navigation links are collections (OpdsFeeds). Language and subject filtering is not
     * supported for collections, so a navigation link matches only when no language or subject is
     * selected and the type filter is empty or includes COLLECTION.
     */
    fun matchesNavigation(): Boolean {
        return languages.isEmpty() && subjects.isEmpty() &&
                (types.isEmpty() || ContentFilterType.COLLECTION in types)
    }

}

/**
 * Get all items in the feed (top level and in groups) that match the given filter.
 */
fun OpdsFeed.itemsMatching(filter: ContentFilterSelection): Set<OpdsFeedSearchMatch> {
    val navigationMatches = filter.matchesNavigation()

    fun matchesIn(
        groupIndex: Int,
        navigation: List<ReadiumLink>,
        publications: List<Publication>,
    ): List<OpdsFeedSearchMatch> {
        val matchingNavigation = if (navigationMatches) {
            navigation.indices.map { OpdsFeedSearchMatch(groupIndex, it, isPublication = false) }
        } else {
            emptyList()
        }

        val matchingPublications = publications.withIndex()
            .filter { filter.matchesPublication(it.value) }
            .map { OpdsFeedSearchMatch(groupIndex, it.index, isPublication = true) }

        return matchingNavigation + matchingPublications
    }

    val topLevelMatches = matchesIn(
        groupIndex = OpdsFeedItemIndex.TOP_LEVEL_GROUP_INDEX,
        navigation = navigation.orEmpty(),
        publications = publications.orEmpty(),
    )

    val groupMatches = groups.orEmpty().flatMapIndexed { groupIndex, group ->
        matchesIn(groupIndex, group.navigation.orEmpty(), group.publications.orEmpty())
    }

    return (topLevelMatches + groupMatches).toSet()
}
