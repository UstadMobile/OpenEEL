package org.openeel.shared.viewmodel.person.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.shared.params.GetListCommonParams
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.phonenumber.OnClickPhoneNumUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.edit
import org.openeel.shared.navigation.ManageAccount
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.PersonDetail
import org.openeel.shared.navigation.PersonEdit
import org.openeel.shared.navigation.CreateAccountSetUsername
import org.openeel.shared.util.ext.asUiText
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.datalayer.db.school.ext.isAdmin
import org.openeel.datalayer.db.school.ext.isAdminOrTeacher
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState
import kotlin.getValue

data class PersonDetailUiState(
    val guid: String = "",
    val persons: DataLoadState<List<Person>> = DataLoadingState(),
    val manageAccountVisible: Boolean = false,
    val createAccountVisible: Boolean = false,
) {

    val person: Person?
        get() = persons.dataOrNull()?.firstOrNull { it.guid == guid }

    val familyMembers: List<Person>
        get() = persons.dataOrNull()?.filter { it.guid != guid } ?: emptyList()

}

class PersonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
    private val onClickPhoneNumUseCase: OnClickPhoneNumUseCase? = null,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent{

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: PersonDetail = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(PersonDetailUiState(guid = route.guid))

    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase by inject()

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    text = Res.string.edit.asUiText(),
                    onClick = ::onClickEdit,
                    icon = FabUiState.FabIcon.EDIT,
                )
            )
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.listAsFlow(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    common = GetListCommonParams(
                        guid = route.guid
                    ),
                    includeRelated = true,
                )
            ).combine(accountManager.selectedAccountAndPersonFlow) { person, activeAccount ->
                Pair(person, activeAccount)
            }.collect { (persons, activeAccount) ->
                val personsVal = persons.dataOrNull()

                val personVal = personsVal?.firstOrNull { it.guid == route.guid }
                val hasAccountPermission = activeAccount?.person?.isAdmin() == true
                        || activeAccount?.person?.guid == personVal?.guid

                val hasWritePermission = checkPersonPermissionUseCase(
                    otherPersonUid = route.guid,
                    otherPersonKnownRole = null,
                    permissionsRequiredByRole = CheckPersonPermissionUseCase.PermissionsRequiredByRole.WRITE_PERMISSIONS,
                )

                _appUiState.update { prev ->
                    prev.copy(
                        title = personVal?.fullName()?.asUiText(),
                        fabState = prev.fabState.copy(
                            visible = hasWritePermission,
                        )
                    )
                }

                _uiState.update { prev ->
                    prev.copy(
                        persons = persons,
                        manageAccountVisible = hasAccountPermission && personVal?.username != null,
                        createAccountVisible = personVal != null &&
                                activeAccount?.person?.isAdminOrTeacher() == true &&
                                personVal.username == null,
                    )
                }
            }
        }
    }

    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PersonEdit.create(route.guid))
        )
    }

    fun onClickCreateAccount() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(CreateAccountSetUsername(route.guid))
        )
    }

    fun navigateToManageAccount() {
        uiState.value.persons.dataOrNull().let {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ManageAccount(guid = route.guid)
                )
            )
        }
    }

    fun onClickPhoneNumber() {
        uiState.value.person?.phoneNumber?.also { phoneNum ->
            onClickPhoneNumUseCase?.invoke(phoneNum)
        }
    }

    fun onClickFamilyMember(guid:String){
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PersonDetail(
                    guid = guid
                )
            )
        )
    }

}