package org.openeel.app.view.catalog.opdssearch

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.openeel.app.components.OpenEelSearchFilterChip
import org.openeel.app.view.catalog.opdsfeeddetail.FeedListItem
import org.openeel.datalayer.school.model.composites.OpdsSearchResult
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal.Companion.TYPE_BOOK
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal.Companion.TYPE_COLLECTION
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal.Companion.TYPE_GAME
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal.Companion.TYPE_VIDEO
import org.openeel.libutil.ext.resolve
import org.openeel.shared.domain.getlanguageendonym.GetLanguageEndonymUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.book
import org.openeel.shared.generated.resources.clear_all
import org.openeel.shared.generated.resources.collection
import org.openeel.shared.generated.resources.game
import org.openeel.shared.generated.resources.language
import org.openeel.shared.generated.resources.subject
import org.openeel.shared.generated.resources.type
import org.openeel.shared.generated.resources.video
import org.openeel.shared.viewmodel.catalog.opdssearch.OpdsSearchUiState
import org.openeel.shared.viewmodel.catalog.opdssearch.OpdsSearchViewModel
import org.openeel.shared.viewmodel.catalog.opdssearch.OpdsSearchViewModel.Companion.TYPE_OPTIONS

@Composable
fun OpdsSearchScreen(
    viewModel: OpdsSearchViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    OpdsSearchScreen(
        uiState = uiState,
        onLanguagesChanged = viewModel::onLanguagesChanged,
        onSubjectsChanged = viewModel::onSubjectsChanged,
        onTypesChanged = viewModel::onTypesChanged,
        onClearFilters = viewModel::onClearFilters,
        onLanguageOptionSearchTextChanged = viewModel::onLanguageOptionSearchTextChanged,
        onSubjectOptionSearchTextChanged = viewModel::onSubjectOptionSearchTextChanged,
        onClickResult = viewModel::onClickResult,
    )
}

@Composable
fun OpdsSearchScreen(
    uiState: OpdsSearchUiState,
    onLanguagesChanged: (List<String>) -> Unit = { },
    onSubjectsChanged: (List<String>) -> Unit = { },
    onTypesChanged: (List<String>) -> Unit = { },
    onClearFilters: () -> Unit = { },
    onLanguageOptionSearchTextChanged: (String) -> Unit = { },
    onSubjectOptionSearchTextChanged: (String) -> Unit = { },
    onClickResult: (OpdsSearchResult) -> Unit = { },
) {
    val getLanguageEndonymUseCase: GetLanguageEndonymUseCase = koinInject()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OpenEelSearchFilterChip(
                    chipLabel = stringResource(Res.string.language),
                    options = uiState.languageOptions,
                    selectedOptions = uiState.searchParams.languages,
                    onSelectedOptionsChanged = onLanguagesChanged,
                    optionLabel = { langCode ->
                        getLanguageEndonymUseCase(langCode)?.let { stringResource(it) } ?: langCode
                    },
                    searchText = uiState.languageOptionSearchText,
                    onSearchTextChanged = onLanguageOptionSearchTextChanged,
                )

                OpenEelSearchFilterChip(
                    chipLabel = stringResource(Res.string.subject),
                    options = uiState.subjectOptions,
                    selectedOptions = uiState.searchParams.subjects,
                    onSelectedOptionsChanged = onSubjectsChanged,
                    optionLabel = { it },
                    searchText = uiState.subjectOptionSearchText,
                    onSearchTextChanged = onSubjectOptionSearchTextChanged,
                )

                OpenEelSearchFilterChip(
                    chipLabel = stringResource(Res.string.type),
                    options = TYPE_OPTIONS,
                    selectedOptions = uiState.searchParams.types,
                    onSelectedOptionsChanged = onTypesChanged,
                    optionLabel = { type ->
                        when (type) {
                            TYPE_COLLECTION -> stringResource(Res.string.collection)
                            TYPE_GAME -> stringResource(Res.string.game)
                            TYPE_BOOK -> stringResource(Res.string.book)
                            TYPE_VIDEO -> stringResource(Res.string.video)
                            else -> type
                        }
                    },
                )
            }

            TextButton(
                onClick = onClearFilters,
                enabled = uiState.hasFilters,
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text(stringResource(Res.string.clear_all))
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(
                items = uiState.results,
                key = { index, _ -> index },
            ) { _, result ->
                val iconUrl = remember(result) {
                    result.iconHref?.let { iconHref ->
                        (result.url ?: result.feedUrl)?.let { Url(it).resolve(iconHref).toString() }
                    }
                }

                FeedListItem(
                    title = result.title,
                    iconUrl = iconUrl,
                    description = null,
                    language = null,
                    duration = null,
                    showCheckbox = false,
                    isSelected = false,
                    onClick = { onClickResult(result) },
                    onLongPress = { },
                )
            }
        }
    }
}
