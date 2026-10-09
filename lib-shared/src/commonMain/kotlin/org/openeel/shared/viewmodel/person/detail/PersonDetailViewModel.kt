package world.respect.shared.viewmodel.person.detail

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
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.phonenumber.OnClickPhoneNumUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.CreateAccountSetUsername
import world.respect.shared.util.ext.asUiText
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
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
    accountManager: RespectAccountManager,
    private val onClickPhoneNumUseCase: OnClickPhoneNumUseCase? = null,
) : RespectViewModel(savedStateHandle), KoinScopeComponent{

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