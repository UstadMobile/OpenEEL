import world.respect.shared.util.SortOrderOption

import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.sort_by
import world.respect.shared.generated.resources.time
import world.respect.shared.generated.resources.title

/**
 * Common reusable sort options for list screens that support sorting by time and/or title.
 *
 * Usage:
 * ```
 * val sortOptions = CommonSortOptions.allOptions()
 * val default = CommonSortOptions.sortByDefault()
 * ```
 */
object CommonSortOptions {

    const val FLAG_TIME_ASC = 1
    const val FLAG_TIME_DESC = 2
    const val FLAG_TITLE_ASC = 3
    const val FLAG_TITLE_DESC = 4

    val TIME_ASC = SortOrderOption(
        fieldMessageId = Res.string.time,
        flag = FLAG_TIME_ASC,
        order = true,
    )

    val TIME_DESC = SortOrderOption(
        fieldMessageId = Res.string.time,
        flag = FLAG_TIME_DESC,
        order = false,
    )

    val TITLE_ASC = SortOrderOption(
        fieldMessageId = Res.string.title,
        flag = FLAG_TITLE_ASC,
        order = true,
    )

    val TITLE_DESC = SortOrderOption(
        fieldMessageId = Res.string.title,
        flag = FLAG_TITLE_DESC,
        order = false,
    )

    /**
     * Default sort option shown initially, displays "Sort by" with no directional arrow.
     * Sorts by time ascending by default.
     */
    val DEFAULT = SortOrderOption(
        fieldMessageId = Res.string.sort_by,
        flag = FLAG_TIME_ASC,
        order = null,
    )

    /**
     * All available sort options for time and title (ascending and descending).
     */
    val ALL_OPTIONS = listOf(
        TIME_ASC,
        TIME_DESC,
        TITLE_ASC,
        TITLE_DESC,
    )
}

