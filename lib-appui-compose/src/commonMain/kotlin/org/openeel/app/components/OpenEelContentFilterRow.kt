package org.openeel.app.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.clear_all

/**
 * A horizontally scrollable row of filter chips with a clear all button. The selection is held by
 * the ViewModel.
 *
 * @param filters the filters to show (e.g. as built using [contentFilterDefinitions]).
 * @param isOptionSelected whether the given option value is selected.
 * @param onToggleOption called when the user checks or unchecks an option.
 * @param clearAllEnabled true when at least one option is selected.
 * @param onClickClearAll called when the user clicks clear all.
 */
@Composable
fun <T> OpenEelContentFilterRow(
    filters: List<OpenEelFilterDefinition<T>>,
    isOptionSelected: (T) -> Boolean,
    onToggleOption: (T) -> Unit,
    clearAllEnabled: Boolean,
    onClickClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filters.isEmpty())
        return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            filters.forEach { filter ->
                key(filter.id) {
                    OpenEelFilterChip(
                        filter = filter,
                        selectedOptionIndices = filter.options.indices.filter {
                            isOptionSelected(filter.options[it].value)
                        },
                        onOptionCheckedChanged = { index, _ ->
                            onToggleOption(filter.options[index].value)
                        },
                    )
                }
            }
        }
        TextButton(
            onClick = onClickClearAll,
            enabled = clearAllEnabled,
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Text(stringResource(Res.string.clear_all))
        }
    }
}

