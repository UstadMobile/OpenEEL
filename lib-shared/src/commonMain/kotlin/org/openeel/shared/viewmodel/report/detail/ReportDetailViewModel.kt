package org.openeel.shared.viewmodel.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.extensions.reportoptions.ReportOptions
import org.openeel.datalayer.school.model.Report
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.report.formatter.CreateGraphFormatterUseCase
import org.openeel.shared.domain.report.formatter.GraphFormatter
import org.openeel.shared.domain.report.query.RunReportUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.edit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ReportDetail
import org.openeel.shared.navigation.ReportEdit
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState

data class ReportDetailUiState(
    val report: Report? = null,
    val reportResult: RunReportUseCase.RunReportResult? = null,
    val errorMessage: String? = null,
    val reportOptions: ReportOptions = ReportOptions(),
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null,
    val subgroupFormatter: GraphFormatter<String>? = null

)

class ReportDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    accountManager: AppAccountManager
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val route: ReportDetail = savedStateHandle.toRoute()
    private val reportUid = route.reportUid
    private val schoolDataSource: SchoolDataSource by inject()
    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: Flow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    fabState = FabUiState(
                        visible = true,
                        text = Res.string.edit.asUiText(),
                        icon = FabUiState.FabIcon.EDIT,
                        onClick = {
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    ReportEdit(reportUid = reportUid)
                                )
                            )
                        },
                    )
                )
            }
        }
        viewModelScope.launch {
            schoolDataSource.reportDataSource.getReportAsFlow(
                route.reportUid
            ).collect { report ->
                _appUiState.update { prev ->
                    prev.copy(
                        title = report.dataOrNull()?.title?.asUiText(),
                    )
                }
                val request = RunReportUseCase.RunReportRequest(
                    reportUid = reportUid.toLong(),
                    reportOptions = report.dataOrNull()?.reportOptions ?: ReportOptions(),
                    accountPersonUid = 0L,
                    timeZoneId = TimeZone.currentSystemDefault().id
                )
                runReportUseCase(request).collect { reportResult ->
                    val xAxisFormatter = createGraphFormatterUseCase(
                        reportResult = reportResult,
                        options = CreateGraphFormatterUseCase.FormatterOptions(
                            paramType = String::class,
                            axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                        )
                    )
                    val subgroupFormatter = createGraphFormatterUseCase(
                        reportResult = reportResult,
                        options = CreateGraphFormatterUseCase.FormatterOptions(
                            paramType = String::class,
                            axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES,
                            forSubgroup = true
                        )
                    )
                    val yAxisFormatter = createGraphFormatterUseCase(
                        reportResult = reportResult,
                        options = CreateGraphFormatterUseCase.FormatterOptions(
                            paramType = Double::class,
                            axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                        )
                    )
                    _uiState.update { prev ->
                        prev.copy(
                            reportResult = reportResult,
                            xAxisFormatter = xAxisFormatter,
                            yAxisFormatter = yAxisFormatter,
                            subgroupFormatter = subgroupFormatter
                        )
                    }
                }
            }
        }
    }
}