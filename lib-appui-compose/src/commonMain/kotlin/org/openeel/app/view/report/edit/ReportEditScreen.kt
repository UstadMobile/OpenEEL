package org.openeel.app.view.report.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelDateField
import org.openeel.app.components.OpenEelExposedDropDownMenuField
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.lib.xapi.extensions.reportoptions.Comparisons
import org.openeel.lib.xapi.extensions.reportoptions.FilterType
import org.openeel.lib.xapi.extensions.reportoptions.FixedReportTimeRange
import org.openeel.lib.xapi.extensions.reportoptions.RelativeRangeReportPeriod
import org.openeel.lib.xapi.extensions.reportoptions.ReportFilter
import org.openeel.lib.xapi.extensions.reportoptions.ReportOptions
import org.openeel.lib.xapi.extensions.reportoptions.ReportPeriodOption
import org.openeel.lib.xapi.extensions.reportoptions.ReportSeries
import org.openeel.lib.xapi.extensions.reportoptions.ReportSeriesVisualType
import org.openeel.lib.xapi.extensions.reportoptions.ReportTimeRangeUnit
import org.openeel.lib.xapi.extensions.reportoptions.ReportXAxis
import org.openeel.lib.xapi.extensions.reportoptions.Indicator
import org.openeel.shared.ext.label
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_filter
import org.openeel.shared.generated.resources.add_series
import org.openeel.shared.generated.resources.chart_type
import org.openeel.shared.generated.resources.filters
import org.openeel.shared.generated.resources.from
import org.openeel.shared.generated.resources.manage_indicators
import org.openeel.shared.generated.resources.quantity
import org.openeel.shared.generated.resources.remove
import org.openeel.shared.generated.resources.series_title
import org.openeel.shared.generated.resources.subgroup_by
import org.openeel.shared.generated.resources.time_range
import org.openeel.shared.generated.resources.title
import org.openeel.shared.generated.resources.to_
import org.openeel.shared.generated.resources.unit
import org.openeel.shared.generated.resources.x_axis
import org.openeel.shared.generated.resources.y_axis
import org.openeel.shared.viewmodel.report.edit.ReportEditUiState
import org.openeel.shared.viewmodel.report.edit.ReportEditViewModel

@Composable
fun ReportEditScreen(
    viewModel: ReportEditViewModel
) {
    val uiState: ReportEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = ReportEditUiState(), context = Dispatchers.Main.immediate
    )
    ReportEditScreen(
        uiState = uiState,
        onReportOptionsChanged = viewModel::onEntityChanged,
        onSeriesChanged = viewModel::onSeriesChanged,
        onAddSeries = viewModel::onAddSeries,
        onAddFilter = viewModel::onAddFilter,
        onRemoveSeries = viewModel::onRemoveSeries,
        onRemoveFilter = viewModel::onRemoveFilter,
        onEditFilter = viewModel::onEditFilter
    )
}

@Composable
private fun ReportEditScreen(
    uiState: ReportEditUiState = ReportEditUiState(),
    onReportOptionsChanged: (ReportOptions) -> Unit = {},
    onAddSeries: () -> Unit = { },
    onAddFilter: (Int) -> Unit = { },
    onSeriesChanged: (Int, ReportSeries) -> Unit = { _, _ -> },
    onRemoveSeries: (Int) -> Unit = { },
    onRemoveFilter: (Int, Int) -> Unit = { _, _ -> },
    onEditFilter: (Int, Int, ReportFilter) -> Unit = { _, _, _ -> },
) {
    val reportOptions = uiState.reportOptions
    val firstIndicatorType = reportOptions.series.firstOrNull()?.reportSeriesYAxis?.type
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = reportOptions.title,
            label = { Text(stringResource(Res.string.title) + "*") },
            onValueChange = { newTitle ->
                onReportOptionsChanged(reportOptions.copy(title = newTitle))
            },
            supportingText = {
                uiState.reportTitleError?.let {
                    Text(uiTextStringResource(it))
                }
            },
            isError = uiState.isTitleError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )

        // Determine selected option based on TIME RANGE TYPE
        val selected = remember(reportOptions.period) {
            when (val currentPeriod = reportOptions.period) {
                is FixedReportTimeRange -> ReportPeriodOption.CUSTOM_DATE_RANGE
                is RelativeRangeReportPeriod -> ReportPeriodOption.entries.find { option ->
                    val optionPeriod = option.period as? RelativeRangeReportPeriod ?: return@find false
                    optionPeriod.rangeUnit == currentPeriod.rangeUnit &&
                            optionPeriod.rangeQuantity == currentPeriod.rangeQuantity
                } ?: ReportPeriodOption.CUSTOM_PERIOD
            }
        }

        OpenEelExposedDropDownMenuField(
            value = selected,
            options = ReportPeriodOption.entries,
            onOptionSelected = { selectedOption ->
                onReportOptionsChanged(reportOptions.copy(period = selectedOption.period))
            },
            label = { Text(stringResource(Res.string.time_range) + "*") },
            itemText = { stringResource(it.label) },
        )

        // Show CustomPeriodInputs only if selected is CUSTOM_PERIOD
        if (selected == ReportPeriodOption.CUSTOM_PERIOD) {
            // When selected is CUSTOM_PERIOD
            CustomPeriodInputs(
                currentRange = reportOptions.period as RelativeRangeReportPeriod,
                onCustomPeriodChanged = { qty, unit ->
                    onReportOptionsChanged(reportOptions.copy(period = RelativeRangeReportPeriod(unit, qty)))
                },
                quantityError = null
            )
        }

        // Show CustomDateRangeInputs only if selected is CUSTOM_DATE_RANGE
        if (selected == ReportPeriodOption.CUSTOM_DATE_RANGE) {
            CustomDateRangeInputs(
                currentRange = reportOptions.period as FixedReportTimeRange,
                onDateRangeChanged = { from, to ->
                    onReportOptionsChanged(reportOptions.copy(period = FixedReportTimeRange(from, to)))
                }
            )
        }

        OpenEelExposedDropDownMenuField(
            value = reportOptions.xAxis,
            options = ReportXAxis.entries,
            onOptionSelected = { xAxis ->
                onReportOptionsChanged(reportOptions.copy(xAxis = xAxis))
            },
            label = { Text(stringResource(Res.string.x_axis) + "*") },
            itemText = { stringResource(it.label) },
        )

        // Dynamically iterate over the series
        reportOptions.series.forEachIndexed { seriesIndex, seriesItem ->
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 1.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Series Title Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = seriesItem.reportSeriesTitle,
                        label = {
                            Text(
                                stringResource(Res.string.series_title) + "*",
                            )
                        },
                        singleLine = true,
                        onValueChange = { newTitle ->
                            val updatedSeries = seriesItem.copy(reportSeriesTitle = newTitle)
                            onSeriesChanged(seriesIndex, updatedSeries)
                        }
                    )
                    if (!uiState.hasSingleSeries) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(Res.string.remove),
                            modifier = Modifier
                                .clickable {
                                    onRemoveSeries(seriesIndex)
                                }
                                .align(Alignment.CenterVertically)
                        )
                    }
                }

                // Y Axis Dropdown
                OpenEelExposedDropDownMenuField(
                    value = seriesItem.reportSeriesYAxis,
                    options = uiState.availableIndicatorsForNewSeries,
                    onOptionSelected = { selectedYAxis ->
                        val updatedSeries = seriesItem.copy(reportSeriesYAxis = selectedYAxis)
                        onSeriesChanged(seriesIndex, updatedSeries)
                    },
                    label = { Text(stringResource(Res.string.y_axis) + "*") },
                    itemText = { stringResource(it.label) },
                )

                // Subgroup Dropdown
                OpenEelExposedDropDownMenuField(
                    value = seriesItem.reportSeriesSubGroup,
                    options = if (reportOptions.xAxis.datePeriod != null) {
                        // X-axis is date - only show non-date options
                        ReportXAxis.entries.filter { it.datePeriod == null }
                    } else {
                        // X-axis is non-date - show date options plus other non-date options
                        ReportXAxis.entries.filter {
                            it.datePeriod != null || (it != reportOptions.xAxis)
                        }
                    },
                    onOptionSelected = { selectedXAxis ->
                        val updatedSeries =
                            seriesItem.copy(reportSeriesSubGroup = selectedXAxis)
                        onSeriesChanged(seriesIndex, updatedSeries)
                    },
                    label = { Text(stringResource(Res.string.subgroup_by)) },
                    itemText = { stringResource(it.label) },
                )

                // Chart Type Dropdown
                OpenEelExposedDropDownMenuField(
                    value = seriesItem.reportSeriesVisualType,
                    options = ReportSeriesVisualType.entries,
                    onOptionSelected = { selectedVisualType ->
                        val updatedSeries =
                            seriesItem.copy(reportSeriesVisualType = selectedVisualType)
                        onSeriesChanged(seriesIndex, updatedSeries)
                    },
                    label = { Text(stringResource(Res.string.chart_type) + "*") },
                    itemText = { stringResource(it.label) },
                )

                if (!seriesItem.reportSeriesFilters.isNullOrEmpty()) {
                    Text(stringResource(Res.string.filters))
                }

                seriesItem.reportSeriesFilters?.forEachIndexed { filterIndex, value ->
                    Row(
                        modifier = Modifier
                            .clickable {
                                onEditFilter(seriesIndex, filterIndex, value)
                            }
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${value.reportFilterField?.let { stringResource(it.label) }} ${value.reportFilterCondition?.symbol} ${value.reportFilterValue}")
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(Res.string.remove),
                            modifier = Modifier
                                .clickable {
                                    onRemoveFilter(seriesIndex, filterIndex)
                                }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    onAddFilter(seriesIndex)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.add_filter),
                )
            }
        }

        OutlinedButton(onClick = { onAddSeries() }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.add_series),
            )
        }
    }
}


@Composable
fun CustomPeriodInputs(
    currentRange: RelativeRangeReportPeriod,
    onCustomPeriodChanged: (Int, ReportTimeRangeUnit) -> Unit,
    quantityError: String?
) {
    var quantity by remember { mutableStateOf(currentRange.rangeQuantity.toString()) }
    var selectedUnit by remember { mutableStateOf(currentRange.rangeUnit) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1.2f),
            value = quantity,
            onValueChange = {
                quantity = it
                it.toIntOrNull()?.let { qty ->
                    onCustomPeriodChanged(qty, selectedUnit)
                }
            },
            label = { Text(stringResource(Res.string.quantity)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = quantityError != null,
            supportingText = { quantityError?.let { Text(it) } },
        )

        OpenEelExposedDropDownMenuField(
            value = selectedUnit,
            modifier = Modifier.weight(0.8f),
            options = ReportTimeRangeUnit.entries,
            onOptionSelected = { unit ->
                selectedUnit = unit
                quantity.toIntOrNull()?.let { qty ->
                    onCustomPeriodChanged(qty, unit)
                }
            },
            itemText = { stringResource(it.label) },
        )
    }
}

@Composable
fun CustomDateRangeInputs(
    currentRange: FixedReportTimeRange,
    onDateRangeChanged: (Long, Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OpenEelDateField(
            modifier = Modifier.weight(1f),
            value = currentRange.fromDateMillis,
            label = { Text(stringResource(Res.string.from)) },
            timeZoneId = TimeZone.currentSystemDefault().id,
            onValueChange = { newFrom ->
                onDateRangeChanged(newFrom, currentRange.toDateMillis)
            },
            supportingText = {}
        )

        OpenEelDateField(
            modifier = Modifier.weight(1f),
            value = currentRange.toDateMillis,
            label = { Text(stringResource(Res.string.to_)) },
            timeZoneId = TimeZone.currentSystemDefault().id,
            onValueChange = { newTo ->
                onDateRangeChanged(currentRange.fromDateMillis, newTo)
            },
            supportingText = {}
        )
    }
}