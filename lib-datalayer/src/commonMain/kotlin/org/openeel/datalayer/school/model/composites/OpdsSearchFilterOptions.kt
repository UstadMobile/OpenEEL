package org.openeel.datalayer.school.model.composites

import kotlinx.serialization.Serializable

/**
 * The options available to filter OPDS search results, based on the cached publications.
 *
 * @property languages distinct language codes from publication metadata.language
 * @property subjects distinct subject names from publication metadata.subject
 */
@Serializable
data class OpdsSearchFilterOptions(
    val languages: List<String> = emptyList(),
    val subjects: List<String> = emptyList(),
)
