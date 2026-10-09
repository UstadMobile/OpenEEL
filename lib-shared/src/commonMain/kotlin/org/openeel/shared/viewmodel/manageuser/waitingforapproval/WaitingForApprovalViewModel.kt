package org.openeel.shared.viewmodel.manageuser.waitingforapproval

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.datalayer.shared.params.GetListCommonParams
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.waiting_title
import org.openeel.shared.navigation.Home
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.RespectAppLauncher
import org.openeel.shared.navigation.SelectClass
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel


data class WaitingForApprovalUiState(
    val className: String = "",
    val isRefreshing: Boolean = false
)

class WaitingForApprovalViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(WaitingForApprovalUiState())

    private val schoolDataSource: SchoolDataSource by inject()

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.waiting_title.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = true,
                showBackButton = false,
            )
        }

        viewModelScope.launch {
            val activeUserUid = accountManager.activeAccount?.userGuid ?: return@launch

            while(true) {
                val personsLoaded = schoolDataSource.personDataSource.list(
                    loadParams = DataLoadParams(),
                    params = PersonDataSource.GetListParams(
                        common = GetListCommonParams(
                            guid = activeUserUid,
                        ),
                        includeRelated = true,
                    )
                ).dataOrNull()

                val personLoaded = personsLoaded?.firstOrNull { it.guid == activeUserUid }
                if (personLoaded?.status == PersonStatusEnum.ACTIVE) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            destination = if (personLoaded.roles.firstOrNull()?.roleEnum == PersonRoleEnum.SHARED_SCHOOL_DEVICE) {
                                SelectClass.create(deviceGuid = personLoaded.guid)
                            } else {
                                Home
                            },
                            clearBackStack = true
                        )
                    )
                    return@launch
                }

                delay(2_000)
            }
        }
    }
}
