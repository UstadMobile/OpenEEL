package org.openeel.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.openeel.lib.opds.model.name
import org.openeel.shared.domain.contentfilter.ContentFilterOption
import org.openeel.shared.domain.contentfilter.ContentFilterOptions
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
 * Language and subject are omitted when there are no options available. Language and subject
 * bottom sheets include a search field (their option lists can be long); type does not.
 */
@Composable
fun contentFilterDefinitions(
    options: ContentFilterOptions,
): List<OpenEelFilterDefinition<ContentFilterOption>> {
    val koin = getKoin()
    val getLanguageEndonymUseCase: GetLanguageEndonymUseCase = remember { koin.get() }

    val languageOptions = options.languages.map { langCode ->
        OpenEelFilterOption<ContentFilterOption>(
            value = ContentFilterOption.Language(langCode),
            label = getLanguageEndonymUseCase(langCode)?.let { stringResource(it) } ?: langCode,
        )
    }.sortedBy { it.label }

    val subjectOptions = options.subjects.map { subject ->
        OpenEelFilterOption<ContentFilterOption>(
            value = ContentFilterOption.Subject(subject),
            label = langMapString(subject.name),
        )
    }.sortedBy { it.label }

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
            searchable = true,
        ).takeIf { languageOptions.isNotEmpty() },
        OpenEelFilterDefinition(
            id = FILTER_ID_SUBJECT,
            label = stringResource(Res.string.subject),
            options = subjectOptions,
            searchable = true,
        ).takeIf { subjectOptions.isNotEmpty() },
        OpenEelFilterDefinition(
            id = FILTER_ID_TYPE,
            label = stringResource(Res.string.type),
            options = typeOptions,
        ),
    )
}

private const val FILTER_ID_LANGUAGE = "language"
private const val FILTER_ID_SUBJECT = "subject"
private const val FILTER_ID_TYPE = "type"
