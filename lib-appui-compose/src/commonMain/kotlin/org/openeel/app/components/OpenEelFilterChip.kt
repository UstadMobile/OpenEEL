package org.openeel.app.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.util.ext.testTagsAsResourceId
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.close
import org.openeel.shared.generated.resources.filter_selected_count
import org.openeel.shared.generated.resources.no_matching_options
import org.openeel.shared.generated.resources.search_filter_options

/**
 * @property value the value represented by the option (e.g. a ContentFilterOption).
 * @property label the text displayed for the option.
 */
data class OpenEelFilterOption<T>(
    val value: T,
    val label: String,
)

/**
 * @property searchable when true, the bottom sheet shows a search field that filters the options
 *           by label (e.g. for long lists such as language and subject).
 */
data class OpenEelFilterDefinition<T>(
    val id: String,
    val label: String,
    val options: List<OpenEelFilterOption<T>>,
    val searchable: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OpenEelFilterChip(
    filter: OpenEelFilterDefinition<T>,
    selectedOptionIndices: List<Int>,
    onOptionCheckedChanged: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSheetOpen by rememberSaveable(filter.id) { mutableStateOf(false) }
    var searchQuery by rememberSaveable(filter.id) { mutableStateOf("") }
    val selectedCount = filter.options.indices.count { it in selectedOptionIndices }

    FilterChip(
        selected = selectedCount > 0,
        onClick = { isSheetOpen = true },
        modifier = modifier,
        label = {
            Text(
                text = if (selectedCount > 0) {
                    stringResource(
                        Res.string.filter_selected_count,
                        filter.label,
                        selectedCount
                    )
                } else {
                    filter.label
                },
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
            )
        },
    )

    if (isSheetOpen) {
        // Keep each option's index in filter.options so selection works on the filtered list
        val visibleOptions = remember(filter.options, searchQuery) {
            val query = searchQuery.trim()
            filter.options.withIndex().filter { (_, option) ->
                query.isEmpty() || option.label.contains(query, ignoreCase = true)
            }
        }

        val dismissSheet = {
            isSheetOpen = false
            searchQuery = ""
        }

        ModalBottomSheet(
            onDismissRequest = dismissSheet,
            modifier = Modifier.testTagsAsResourceId(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    text = filter.label,
                )

                IconButton(
                    onClick = dismissSheet,
                    modifier = Modifier.testTag("close_filter_bottom_sheet_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.close),
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp)

            if (filter.searchable) {
                OpenEelSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = stringResource(Res.string.search_filter_options, filter.label),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding()
                        .testTag("filter_search_field"),
                )
            }

            if (visibleOptions.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_matching_options),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(visibleOptions, key = { it.index }) { (index, option) ->
                    val isSelected = index in selectedOptionIndices
                    OpenEelBottomSheetOption(
                        headlineContent = { Text(option.label) },
                        trailingContent = {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isSelected,
                                role = Role.Checkbox,
                                onValueChange = { onOptionCheckedChanged(index, it) },
                            ),
                    )
                }
            }
        }
    }
}
