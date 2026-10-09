package world.respect.shared.viewmodel.person.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.paging.EmptyPagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.PagingSourceFactoryHolder
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.ext.resultExpected
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_new_person
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.generated.resources.people
import world.respect.shared.generated.resources.select_person
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
import world.respect.datalayer.school.domain.GetWritableRolesListUseCase
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import world.respect.shared.ext.tryOrShowSnackbarOnError
import world.respect.shared.viewmodel.app.appstate.ExpandableFabIcon
import world.respect.shared.viewmodel.app.appstate.ExpandableFabItem
import world.respect.shared.viewmodel.app.appstate.ExpandableFabUiState
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher


data class PersonListUiState(
    val persons: IPagingSourceFactory<Int, PersonListDetails> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val showAddPersonItem: Boolean = false,
    val showInvitePersonItem: Boolean = false,
    val isPendingExpanded: Boolean = true,
    val showInvite: Boolean = false,
    val pendingPersons: IPagingSourceFactory<Int, Person> =
        IPagingSourceFactory { EmptyPagingSource() },
    val writableRoles: List<PersonRoleEnum> = emptyList(),
) {


    fun showApproveOption(role: PersonRoleEnum): Boolean {
        return role in writableRoles
    }

}

class PersonListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(PersonListUiState())

    val uiState = _uiState.asStateFlow()

    private val launchDebounced = LaunchDebouncer(viewModelScope)

    private val route: PersonList = savedStateHandle.toRoute()

    private val getWritableRolesListUseCase: GetWritableRolesListUseCase by inject()

    private val approveOrDeclineInviteRequestUseCase: ApproveOrDeclineInviteRequestUseCase by inject()

    private val pendingPersonsPagingSource = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByPersonStatus = PersonStatusEnum.PENDING_APPROVAL,
            )
        )
    }

    private val pagingSourceFactoryHolder = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listDetailsAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByName = _appUiState.value.searchState.searchText.takeIf { it.isNotBlank() },
                filterByPersonRole = route.filterByRole,
                filterByPersonStatus = PersonStatusEnum.ACTIVE,
            )
        )
    }

    init {
        _appUiState.update {
            it.copy(
                title = if(!route.resultExpected) {
                    Res.string.people.asUiText()
                }else {
                    Res.string.select_person.asUiText()
                },
                expandableFabState = ExpandableFabUiState(
                    visible = false,
                    items = listOf(
                        ExpandableFabItem(
                            icon = ExpandableFabIcon.INVITE,
                            text =  Res.string.invite_person.asUiText(),
                            onClick = ::onClickInvitePerson,
                        ),
                        ExpandableFabItem(
                            icon = ExpandableFabIcon.ADD,
                            text = Res.string.add_new_person.asUiText(),
                            onClick = ::onClickAdd,
                        )
                    )
                ),
                searchState = AppBarSearchUiState(
                    visible = true,
                    searchText = "",
                    onSearchTextChanged = ::onSearchTextChanged
                ),
                showBackButton = route.resultExpected,
                hideBottomNavigation = route.resultExpected,
                userAccountIconVisible = !route.resultExpected,
            )
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selectedAcct ->
                val writableRoles = selectedAcct?.person?.roles?.firstOrNull()?.let {
                    getWritableRolesListUseCase(it.roleEnum)
                } ?: emptyList()

                val canAddPerson = writableRoles.isNotEmpty()
                _uiState.update { it.copy(writableRoles = writableRoles) }

                val canInvitePerson = canAddPerson || route.inviteUid != null

                _appUiState.update { prev ->
                    prev.copy(
                        expandableFabState = prev.expandableFabState.copy(
                            visible = canAddPerson && !route.resultExpected
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        showAddPersonItem = canAddPerson && route.resultExpected,
                        showInvitePersonItem = !route.hideInvite && canInvitePerson
                                && route.resultExpected,
                    )
                }
            }
        }

        _uiState.update {
            it.copy(
                pendingPersons = pendingPersonsPagingSource,
                persons = pagingSourceFactoryHolder,
                showInvite = !route.hideInvite && (route.filterByRole != null||route.addToClassUid!=null)
            )
        }
    }
    fun onTogglePendingInvites() {
        _uiState.update {
            it.copy(isPendingExpanded = !it.isPendingExpanded)
        }
    }

    fun onSearchTextChanged(text: String) {
        _appUiState.update {
            it.copy(
                searchState = it.searchState.copy(
                    searchText = text
                )
            )
        }

        launchDebounced.launch("") {
            pagingSourceFactoryHolder.invalidate()
        }
    }

    fun onClickItem(person: PersonListDetails) {
        if(route.resultExpected) {
            viewModelScope.launch {
                resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = schoolDataSource.personDataSource.findByGuid(
                        loadParams = DataLoadParams(),
                        guid = person.guid,
                    ).dataOrNull(),
                )
            }
        }else {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(PersonDetail(person.guid))
            )
        }
    }


    fun onClickAcceptOrDismissInvite(
        person: Person,
        approved: Boolean,
    ) {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError {
                approveOrDeclineInviteRequestUseCase(
                    personUid = person.guid,
                    approved = approved,
                )
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PersonEdit.create(
                    null,
                    resultDest = route.resultDest,
                    presetRole = route.filterByRole
                )
            )
        )
    }

    fun onClickInvitePerson() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                InvitePerson.create(
                    invitePersonOptions = if(route.inviteUid != null) {
                        InvitePerson.ClassInviteOptions(
                            inviteUid = route.inviteUid
                        )
                    }else {
                        InvitePerson.NewUserInviteOptions(
                            presetRole = route.filterByRole
                        )
                    }
                )
            )
        )
    }

}