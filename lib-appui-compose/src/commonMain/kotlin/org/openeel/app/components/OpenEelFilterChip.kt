package org.openeel.app.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
 * @property selectedCount the number of options selected (including options not currently shown
 *           because they do not match the search text).
 * @property searchText the text entered to search the options.
 * @property onSearchTextChanged when not null, the bottom sheet shows a search field (e.g. for long
 *           lists such as language and subject). The search itself is done by the ViewModel, which
 *           provides the matching [options].
 */
data class OpenEelFilterDefinition<T>(
    val id: String,
    val label: String,
    val options: List<OpenEelFilterOption<T>>,
    val selectedCount: Int = 0,
    val searchText: String = "",
    val onSearchTextChanged: ((String) -> Unit)? = null,
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
    val selectedCount = filter.selectedCount

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
        val dismissSheet: () -> Unit = {
            isSheetOpen = false
            filter.onSearchTextChanged?.invoke("")
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

            filter.onSearchTextChanged?.also { onSearchTextChanged ->
                OpenEelSearchField(
                    value = filter.searchText,
                    onValueChange = onSearchTextChanged,
                    placeholder = stringResource(Res.string.search_filter_options, filter.label),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding()
                        .testTag("filter_search_field"),
                )
            }

            if (filter.options.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_matching_options),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(filter.options) { index, option ->
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
