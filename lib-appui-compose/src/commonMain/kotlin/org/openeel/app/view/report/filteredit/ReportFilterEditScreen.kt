package world.respect.app.view.report.filteredit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.view.report.edit.ExposedDropdownMenu
import world.respect.lib.xapi.extensions.reportoptions.FilterType
import world.respect.lib.xapi.extensions.reportoptions.GenderType
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.condition
import world.respect.shared.generated.resources.field
import world.respect.shared.generated.resources.value
import world.respect.shared.viewmodel.report.filteredit.ReportFilterEditUiState
import world.respect.shared.viewmodel.report.filteredit.ReportFilterEditViewModel

@Composable
fun ReportFilterEditScreen(
    navController: NavHostController,
    viewModel: ReportFilterEditViewModel
) {
    val uiState: ReportFilterEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = ReportFilterEditUiState(),
        context = Dispatchers.Main.immediate
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ExposedDropdownMenu(
                selectedValue = uiState.filters?.reportFilterField,
                label = { Text(stringResource(Res.string.field) + "*") },
                options = FilterType.entries,
                onOptionSelected = { selectedOption ->
                    val updatedOptions = uiState.filters?.copy(
                        reportFilterField = selectedOption,
                        reportFilterValue = null,
                        reportFilterCondition = null
                    )
                    viewModel.onEntityChanged(updatedOptions)
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenu(
                    modifier = Modifier.weight(0.8f),
                    label = { Text(stringResource(Res.string.condition) + "*") },
                    selectedValue = uiState.filters?.reportFilterCondition,
                    options = uiState.filterConditionOptions?.comparisonTypes ?: emptyList(),
                    onOptionSelected = { selectedOption ->
                        val updatedOptions = uiState.filters?.copy(
                            reportFilterCondition = selectedOption
                        )
                        viewModel.onEntityChanged(updatedOptions)
                    }
                )

                when (uiState.filters?.reportFilterField) {
                    FilterType.PERSON_GENDER -> {
                        ExposedDropdownMenu(
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(Res.string.value) + "*") },
                            selectedValue = GenderType.entries.firstOrNull {
                                it.name == uiState.filters?.reportFilterValue
                            },
                            options = GenderType.entries,
                            onOptionSelected = { selectedGender ->
                                val updatedFilter = uiState.filters?.copy(
                                    reportFilterValue = selectedGender.name
                                )
                                viewModel.onEntityChanged(updatedFilter)
                            }
                        )
                    }

                    else -> {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(Res.string.value) + "*") },
                            value = uiState.filters?.reportFilterValue ?: "",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            onValueChange = {
                                val updatedOptions = uiState.filters?.copy(
                                    reportFilterValue = it
                                )
                                viewModel.onEntityChanged(updatedOptions)
                            }
                        )
                    }
                }
            }
        }
    }
}