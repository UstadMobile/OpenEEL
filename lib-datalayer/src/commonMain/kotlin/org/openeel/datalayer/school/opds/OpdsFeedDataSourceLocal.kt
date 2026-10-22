package org.openeel.datalayer.school.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.networkvalidation.BaseDataSourceValidationHelper
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
     */
    data class SearchParams(
        val searchText: String = "",
        val languages: List<String> = emptyList(),
        val subjects: List<String> = emptyList(),
        val types: List<String> = emptyList(),
    )

    /**
     * Search all cached publications and navigation links (from every feed stored locally).
     */
    fun searchAsFlow(
        params: SearchParams,
    ): Flow<List<OpdsSearchResult>>

    /**
     * Get the distinct languages (metadata.language) of cached publications available to filter
     * search results.
     *
     * @param searchText text to search for in language codes; when blank all match.
     */
    fun getLanguageOptionsAsFlow(
        searchText: String = "",
    ): Flow<List<String>>

    /**
     * Get the distinct subject names (metadata.subject) of cached publications available to
     * filter search results.
     *
     * @param searchText text to search for in subject names; when blank all match.
     */
    fun getSubjectOptionsAsFlow(
        searchText: String = "",
    ): Flow<List<String>>

    /**
     * The update local is a little different for OpdsFeed because the data can come from different
     * servers. External servers may set the etag and last-modified any way they wish, so we need
     * the DataReadyState to access metadata.
     */
    suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<OpdsFeed>,
    )

    companion object{
        const val TYPE_COLLECTION = "http://schema.org/Collection"
        const val TYPE_GAME = "http://schema.org/Game"
        const val TYPE_BOOK = "http://schema.org/Book"
        const val TYPE_VIDEO = "http://schema.org/VideoObject"
    }
}
