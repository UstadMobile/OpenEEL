package world.respect.shared.viewmodel.manageuser.accountlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.ext.primaryRole
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.libutil.ext.replaceOrAppend
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSession
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.accounts
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.Home
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.SelectClass
import world.respect.shared.navigation.ShareFeedback
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.isSameAccount
import world.respect.shared.viewmodel.RespectViewModel

/**
 * @property selectedAccount if not null, the currently selected account
 * @property accounts other accounts that are signed-in, available, and the user can switch to (
 *           (not including the selectedAccount)
 */
data class AccountListUiState(
    val selectedAccount: RespectSessionAndPerson? = null,
    val accounts: List<RespectSessionAndPerson> = emptyList(),
    val accountOwnerRole: PersonRoleEnum? = null,
) {
    val showSelectedAccountProfileButton: Boolean
        get() = selectedAccount?.person?.status != PersonStatusEnum.PENDING_APPROVAL
                && accountOwnerRole != PersonRoleEnum.SHARED_SCHOOL_DEVICE // Hide for shared device

    val familyMembersClickEnabled: Boolean
        get() = selectedAccount?.person?.status != PersonStatusEnum.PENDING_APPROVAL

}

class AccountListViewModel(
    private val respectAccountManager: RespectAccountManager,
    savedStateHandle: SavedStateHandle
) : RespectViewModel(savedStateHandle){

    private val _uiState = MutableStateFlow(AccountListUiState())

    val uiState = _uiState.asStateFlow()

    private var emittedNavToGetStartedCommand = false
    init {
        _appUiState.update {
            it.copy(
                title = Res.string.accounts.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }

        viewModelScope.launch {
            respectAccountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                val accountOwnerRole = respectAccountManager.activeAccount?.let { account ->
                    val accountScope = respectAccountManager.getOrCreateAccountScope(account)
                    val dataSource: SchoolDataSource = accountScope.get()
                    dataSource.personDataSource.findByGuid(
                        DataLoadParams(),
                        account.userGuid
                    ).dataOrNull()?.primaryRole()
                }

                _uiState.update { prev ->
                    prev.copy(
                        selectedAccount = accountAndPerson,
                        accountOwnerRole = accountOwnerRole
                    )
                }
            }
        }

        viewModelScope.launch {
            respectAccountManager.accounts.combine(
                respectAccountManager.selectedAccountFlow
            ) { storedAccounts, activeAccount ->
                Pair(storedAccounts, activeAccount)
            }.collectLatest { (storedAccounts, activeAccount) ->
                /*
                 * If there are no stored accounts (eg because they have logged out of all accounts),
                 * or if a session is terminated remotely (eg password reset), then must go to
                 * GetStarted screen.
                 */
                if(storedAccounts.isEmpty() && !emittedNavToGetStartedCommand) {
                    emittedNavToGetStartedCommand = true
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            GetStartedScreen(), clearBackStack = true
                        )
                    )

                    return@collectLatest
                }

                //As noted on UiState - the active account is removed from the list of other
                //accounts
                val storedAccountList = storedAccounts.filterNot {
                    activeAccount?.isSameAccount(it) == true
                }

                _uiState.update { prev ->
                    prev.copy(
                        accounts = storedAccountList.map {
                            RespectSessionAndPerson(
                                session = RespectSession(it, null),
                                person = Person(
                                    guid = it.userGuid,
                                    givenName = "",
                                    familyName = "",
                                    roles = emptyList(),
                                    gender = PersonGenderEnum.UNSPECIFIED,
                                )
                            )
                        }
                    )
                }

                storedAccountList.forEach { account ->
                    launch {
                        val accountScope = respectAccountManager.getOrCreateAccountScope(account)
                        val dataSource: SchoolDataSource = accountScope.get()
                        dataSource.personDataSource.findByGuidAsFlow(
                            account.userGuid
                        ).collect { person ->
                            _uiState.update { prev ->
                                prev.copy(
                                    accounts = prev.accounts.replaceOrAppend(
                                        RespectSessionAndPerson(
                                            session = RespectSession(account, null),
                                            person = person.dataOrNull() ?: Person(
                                                guid = account.userGuid,
                                                givenName = "",
                                                familyName = "",
                                                roles = emptyList(),
                                                gender = PersonGenderEnum.UNSPECIFIED,
                                            )
                                        )
                                    ) {
                                        it.session.account.isSameAccount(account)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onClickAccount(account: RespectAccount) {
        respectAccountManager.switchAccount(account)

        viewModelScope.launch {
            val accountScope = respectAccountManager.getOrCreateAccountScope(account)
            val person = accountScope.get<SchoolDataSource>().personDataSource.findByGuid(
                loadParams = DataLoadParams(onlyIfCached = true),
                guid = account.userGuid
            )

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = if(person.dataOrNull()?.status != PersonStatusEnum.PENDING_APPROVAL) {
                        Home
                    }else {
                        WaitingForApproval()
                    },
                    clearBackStack = true
                )
            )
        }


    }

    fun onClickFamilyPerson(person: Person) {
        viewModelScope.launch {
            respectAccountManager.switchProfile(person.guid)
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = if(person.roles.firstOrNull()?.roleEnum == PersonRoleEnum.PARENT) {
                        Home
                    } else {
                        AssignmentList
                    },
                    clearBackStack = true
                )
            )
        }
    }

    fun onClickAddAccount() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(GetStartedScreen(canGoBack = true))
        )
    }

    fun onClickProfile() {
        uiState.value.selectedAccount?.also {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PersonDetail(
                        guid = it.session.account.userGuid
                    )
                )
            )
        }
    }


    fun onClickLogout() {
        val isSelectedAccountSharedDevice =
            uiState.value.accountOwnerRole == PersonRoleEnum.SHARED_SCHOOL_DEVICE

        // Find the shared device account in the accounts list
        val sharedDeviceAccount = uiState.value.accounts.find { andPerson ->
            andPerson.person.roles.any { role ->
                role.roleEnum == PersonRoleEnum.SHARED_SCHOOL_DEVICE
            }
        }

        if (isSelectedAccountSharedDevice) {
            // Currently on shared device account - just go to Select Class
            uiState.value.selectedAccount?.also { selectedAccount ->
                viewModelScope.launch {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            destination = SelectClass.create(deviceGuid = selectedAccount.person.guid),
                            clearBackStack = true
                        )
                    )
                }
            }
        } else if (sharedDeviceAccount?.person?.status == PersonStatusEnum.ACTIVE) {
            // This is a teacher/admin on a shared device - switch to shared device account first
            viewModelScope.launch {
                // First switch to the shared device account
                respectAccountManager.switchAccount(sharedDeviceAccount.session.account)
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = SelectClass.create(deviceGuid = sharedDeviceAccount.person.guid),
                        clearBackStack = true
                    )
                )
            }
        } else {
            // Regular logout - no shared device present
            uiState.value.selectedAccount?.also {
                viewModelScope.launch {
                    respectAccountManager.removeAccount(it.session.account)
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            destination = GetStartedScreen(),
                            clearBackStack = true
                        )
                    )
                }
            }
        }
    }

    fun onClickShareFeedback(){
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ShareFeedback)
        )
    }
}