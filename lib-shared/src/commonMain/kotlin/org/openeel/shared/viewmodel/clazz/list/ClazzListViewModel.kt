package org.openeel.shared.viewmodel.clazz.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.shared.paging.EmptyPagingSourceFactory
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.classes
import org.openeel.shared.generated.resources.clazz
import org.openeel.shared.generated.resources.first_name
import org.openeel.shared.generated.resources.last_name
import org.openeel.shared.navigation.ClazzDetail
import org.openeel.shared.navigation.ClazzEdit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.SortOrderOption
import org.openeel.shared.util.ext.asUiText
import org.openeel.datalayer.school.model.PermissionFlags
import org.openeel.datalayer.school.writequeue.EnqueueRunPullSyncUseCase
import org.openeel.shared.domain.permissions.CheckSchoolPermissionsUseCase
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState

data class ClazzListUiState(
    val classes: IPagingSourceFactory<Int, Clazz> = EmptyPagingSourceFactory(),
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.first_name, 1, true
    ),
    val fieldsEnabled: Boolean = true
)

class ClazzListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val checkSchoolPermissionsUseCase: CheckSchoolPermissionsUseCase by inject()

    private val _uiState = MutableStateFlow(ClazzListUiState())

    val uiState = _uiState.asStateFlow()

    private val enqueuePullSyncUseCase: EnqueueRunPullSyncUseCase by inject()

    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.classDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = ClassDataSource.GetListParams()
        )
    }

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.classes.asUiText(),
                fabState = it.fabState.copy(
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.clazz.asUiText(),
                    onClick = ::onClickAdd
                ),
                showBackButton = false,
            )
        }

        _uiState.update { prev ->
            prev.copy(
                classes = pagingSourceHolder,
                sortOptions = listOf(
                    SortOrderOption(
                        Res.string.first_name,
                        flag = 1,
                        order = true
                    ),
                    SortOrderOption(
                        Res.string.last_name,
                        flag = 2,
                        order = true
                    )
                )
            )
        }

        viewModelScope.launch {
            enqueuePullSyncUseCase()

            val canAddClass = checkSchoolPermissionsUseCase(
                listOf(PermissionFlags.CLASS_WRITE)
            ).isNotEmpty()

            _appUiState.update { prev ->
                prev.copy(
                    fabState = prev.fabState.copy(
                        visible = canAddClass
                    )
                )
            }
        }
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update {
            it.copy(activeSortOrderOption = sortOption)
        }
    }

    fun onClickClazz(clazz: Clazz) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ClazzDetail(clazz.guid)
            )
        )
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ClazzEdit(guid = null))
        )
    }
}

