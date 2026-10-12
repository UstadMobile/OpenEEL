package org.openeel.app.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.filter_selected_count

/**
 * @property value the value represented by the option (e.g. a ContentFilterOption).
 * @property label the text displayed for the option.
 */
data class OpenEelFilterOption<T>(
    val value: T,
    val label: String,
)

data class OpenEelFilterDefinition<T>(
    val id: String,
    val label: String,
    val options: List<OpenEelFilterOption<T>>,
)

@Composable
fun <T> OpenEelFilterChip(
    filter: OpenEelFilterDefinition<T>,
    selectedOptionIndices: List<Int>,
    onOptionCheckedChanged: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSheetOpen by rememberSaveable(filter.id) { mutableStateOf(false) }
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
        OpenEelBottomSheet(
            title = filter.label,
            onDismissRequest = { isSheetOpen = false },
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(filter.options, key = { index, _ -> index }) { index, option ->
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
