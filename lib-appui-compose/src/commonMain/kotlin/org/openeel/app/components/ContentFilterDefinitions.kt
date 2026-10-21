package org.openeel.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.openeel.datalayer.school.model.composites.OpdsSearchFilterOptions
import org.openeel.shared.domain.contentfilter.ContentFilterOption
import org.openeel.shared.domain.contentfilter.ContentFilterType
import org.openeel.shared.domain.getlanguageendonym.GetLanguageEndonymUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.language
import org.openeel.shared.generated.resources.subject
import org.openeel.shared.generated.resources.type

/**
 * Build the content filter chips.
 *
 * Language: options from publication metadata.language, displayed as endonyms using
 * GetLanguageEndonymUseCase (as per AppDetail and PublicationDetail).
 * Subject: options from publication metadata.subject.
 * Type: hardcoded options (see [ContentFilterType]); always shown.
 *
 * Language and subject options are searched (and sorted) by the database: [options] contains only
 * the options matching [languageSearchText] / [subjectSearchText]. [showLanguageFilter] and
 * [showSubjectFilter] control whether those chips are shown at all.
 **/
@Composable
fun contentFilterDefinitions(
    options: OpdsSearchFilterOptions,
    selectedFilters: Set<ContentFilterOption>,
    languageSearchText: String,
    onLanguageSearchTextChanged: (String) -> Unit,
    subjectSearchText: String,
    onSubjectSearchTextChanged: (String) -> Unit,
    showLanguageFilter: Boolean,
    showSubjectFilter: Boolean,
): List<OpenEelFilterDefinition<ContentFilterOption>> {
    val koin = getKoin()
    val getLanguageEndonymUseCase: GetLanguageEndonymUseCase = remember { koin.get() }

    val languageOptions = options.languages.map { langCode ->
        OpenEelFilterOption<ContentFilterOption>(
            value = ContentFilterOption.Language(langCode),
            label = getLanguageEndonymUseCase(langCode)?.let { stringResource(it) } ?: langCode,
        )
    }

    val subjectOptions = options.subjects.map { subject ->
        OpenEelFilterOption<ContentFilterOption>(
            value = ContentFilterOption.Subject(subject),
            label = subject,
        )
    }

    val typeOptions = ContentFilterType.entries.map { type ->
        OpenEelFilterOption<ContentFilterOption>(
            value = ContentFilterOption.Type(type),
            label = stringResource(type.label),
        )
    }

    return listOfNotNull(
        OpenEelFilterDefinition(
            id = FILTER_ID_LANGUAGE,
            label = stringResource(Res.string.language),
            options = languageOptions,
            selectedCount = selectedFilters.count { it is ContentFilterOption.Language },
            searchText = languageSearchText,
            onSearchTextChanged = onLanguageSearchTextChanged,
        ).takeIf { showLanguageFilter },
        OpenEelFilterDefinition(
            id = FILTER_ID_SUBJECT,
            label = stringResource(Res.string.subject),
            options = subjectOptions,
            selectedCount = selectedFilters.count { it is ContentFilterOption.Subject },
            searchText = subjectSearchText,
            onSearchTextChanged = onSubjectSearchTextChanged,
        ).takeIf { showSubjectFilter },
        OpenEelFilterDefinition(
            id = FILTER_ID_TYPE,
            label = stringResource(Res.string.type),
            options = typeOptions,
            selectedCount = selectedFilters.count { it is ContentFilterOption.Type },
        ),
    )
}

private const val FILTER_ID_LANGUAGE = "language"
private const val FILTER_ID_SUBJECT = "subject"
private const val FILTER_ID_TYPE = "type"
