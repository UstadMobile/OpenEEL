package org.openeel.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.ascending
import org.openeel.shared.generated.resources.descending
import org.openeel.shared.util.SortOrderOption
import org.openeel.shared.util.description

enum class SortListMode {

    POPUP, BOTTOM_SHEET

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectListSortHeader(
    activeSortOrderOption: SortOrderOption,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    mode:SortListMode = defaultSortListMode(),
    sortOptions: List<SortOrderOption> = emptyList(),
    onClickSortOption: (SortOrderOption) -> Unit = { },
){
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
    ) {
        TextButton(
            enabled = enabled,
            onClick = {
                expanded = true
            }
        ) {
            Text(stringResource(resource = activeSortOrderOption.fieldMessageId))

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = if(activeSortOrderOption.order != false)
                    Icons.Default.ArrowDownward
                else
                    Icons.Default.ArrowUpward,
                contentDescription = when(activeSortOrderOption.order) {
                    null -> null
                    true -> stringResource(Res.string.ascending)
                    false ->stringResource(Res.string.descending)
                },
                modifier = Modifier.size(16.dp)
            )
            when(mode) {
                SortListMode.POPUP -> {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        sortOptions.forEach { sortOption ->
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    onClickSortOption(sortOption)
                                },
                                text = {
                                    Text(sortOption.description())
                                }
                            )
                        }
                    }
                }
                SortListMode.BOTTOM_SHEET -> {
                    if(expanded) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            RespectSortOptionsBottomSheet(
                                sortOptions = sortOptions,
                                onClickSortOption = onClickSortOption,
                                onDismissRequest = {
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun defaultSortListMode() : SortListMode = SortListMode.BOTTOM_SHEET
