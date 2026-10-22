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
 * A filter chip that opens a bottom sheet where the user can select one or more options. The
 * options and selection are held by the ViewModel.
 *
 * @param chipLabel the label shown on the chip and as the bottom sheet title.
 * @param options the options the user can select from.
 * @param selectedOptions the options currently selected (may include options not in [options] e.g.
 *        when they do not match the search text).
 * @param onSelectedOptionsChanged called with the new list of selected options.
 * @param optionLabel the text displayed for an option.
 * @param searchText the text entered to search the options.
 * @param onSearchTextChanged when not null, the bottom sheet shows a search field (e.g. for long
 *        lists such as language and subject). The search itself is done by the ViewModel, which
 *        provides the matching [options].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OpenEelSearchFilterChip(
    chipLabel: String,
    options: List<T>,
    selectedOptions: List<T>,
    onSelectedOptionsChanged: (List<T>) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    searchText: String = "",
    onSearchTextChanged: ((String) -> Unit)? = null,
) {
    var sheetExpanded by remember { mutableStateOf(false) }

    FilterChip(
        selected = selectedOptions.isNotEmpty(),
        onClick = { sheetExpanded = true },
        modifier = modifier,
        label = {
            Text(
                text = if (selectedOptions.isNotEmpty()) {
                    stringResource(
                        Res.string.filter_selected_count,
                        chipLabel,
                        selectedOptions.size
                    )
                } else {
                    chipLabel
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

    if (sheetExpanded) {
        val dismissSheet: () -> Unit = {
            sheetExpanded = false
            onSearchTextChanged?.invoke("")
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
                    text = chipLabel,
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

            onSearchTextChanged?.also {
                OpenEelSearchField(
                    value = searchText,
                    onValueChange = it,
                    placeholder = stringResource(Res.string.search_filter_options, chipLabel),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultItemPadding()
                        .testTag("filter_search_field"),
                )
            }

            if (options.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_matching_options),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(options) { option ->
                    val isSelected = option in selectedOptions
                    OpenEelBottomSheetOption(
                        headlineContent = { Text(optionLabel(option)) },
                        trailingContent = {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isSelected,
                                role = Role.Checkbox,
                                onValueChange = { checked ->
                                    onSelectedOptionsChanged(
                                        if (checked) {
                                            selectedOptions + option
                                        } else {
                                            selectedOptions - option
                                        }
                                    )
                                },
                            ),
                    )
                }
            }
        }
    }
}
