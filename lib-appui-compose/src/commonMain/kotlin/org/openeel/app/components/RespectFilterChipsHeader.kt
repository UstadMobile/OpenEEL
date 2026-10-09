package world.respect.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> RespectFilterChipsHeader(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: @Composable ((T) -> String),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            FilterChip(
                selected = isSelected,
                onClick = {
                    onOptionSelected(option)
                },
                label = {
                    Text(
                        text = optionLabel(option),
                    )
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedOption == option,
                )

            )
        }
    }
}
