package org.openeel.datalayer.school.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.networkvalidation.BaseDataSourceValidationHelper
import org.openeel.datalayer.school.model.composites.OpdsSearchFilterOptions
import org.openeel.datalayer.school.model.composites.OpdsSearchResult
import org.openeel.lib.opds.model.OpdsFeed

interface OpdsFeedDataSourceLocal: OpdsFeedDataSource, BaseDataSourceValidationHelper {

    /**
     * Parameters used to search all cached OPDS publications and navigation links. Within each
     * filter (languages, subjects, types) options are combined using OR, different filters are
     * combined using AND. An empty list means the filter is not applied.
     *
     * @property searchText text to search for in titles; when blank all items match.
     * @property languages publication language codes (metadata.language) to match.
     * @property subjects publication subject names (metadata.subject) to match.
     * @property types publication schema.org types (metadata.@type) to match.
     * @property includePublications whether publications should be included.
     * @property includeNavigation whether navigation links (collections) should be included.
     */
    data class SearchParams(
        val searchText: String = "",
        val languages: List<String> = emptyList(),
        val subjects: List<String> = emptyList(),
        val types: List<String> = emptyList(),
        //Convert these to using a property with no backing field: same reason as mentioned on UiState
        val includePublications: Boolean = true,
        val includeNavigation: Boolean = true,
    )

    /**
     * Search all cached publications and navigation links (from every feed stored locally).
     */
    fun searchAsFlow(
        params: SearchParams,
    ): Flow<List<OpdsSearchResult>>

    /**
     * Get the languages and subjects available to filter search results.
     *
     * @param languageSearchText text to search for in language codes; when blank all match.
     * @param subjectSearchText text to search for in subject names; when blank all match.
     */
    fun getSearchFilterOptionsAsFlow(
        languageSearchText: String = "",
        subjectSearchText: String = "",
    ): Flow<OpdsSearchFilterOptions>

    /**
     * The update local is a little different for OpdsFeed because the data can come from different
     * servers. External servers may set the etag and last-modified any way they wish, so we need
     * the DataReadyState to access metadata.
     */
    suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<OpdsFeed>,
    )

}
