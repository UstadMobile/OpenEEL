package org.openeel.shared.viewmodel.manageuser.accountlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.libutil.ext.replaceOrAppend
import org.openeel.shared.domain.account.UserAccount
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.account.UserSession
import org.openeel.shared.domain.account.UserSessionAndPerson
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.accounts
import org.openeel.shared.navigation.AssignmentList
import org.openeel.shared.navigation.GetStartedScreen
import org.openeel.shared.navigation.Home
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.PersonDetail
import org.openeel.shared.navigation.ShareFeedback
import org.openeel.shared.navigation.WaitingForApproval
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.isSameAccount
import org.openeel.shared.viewmodel.OpenEelViewModel

/**
 * @property selectedAccount if not null, the currently selected account
 * @property accounts other accounts that are signed-in, available, and the user can switch to (
 *           (not including the selectedAccount)
 */
data class AccountListUiState(
    val selectedAccount: UserSessionAndPerson? = null,
    val accounts: List<UserSessionAndPerson> = emptyList(),
) {
    val showSelectedAccountProfileButton: Boolean
        get() = selectedAccount?.person?.status != PersonStatusEnum.PENDING_APPROVAL

    val familyMembersClickEnabled: Boolean
        get() = selectedAccount?.person?.status != PersonStatusEnum.PENDING_APPROVAL

}

class AccountListViewModel(
    private val appAccountManager: AppAccountManager,
    savedStateHandle: SavedStateHandle
) : OpenEelViewModel(savedStateHandle){

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
            appAccountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update { prev ->
                    prev.copy(selectedAccount = accountAndPerson)
                }
            }
        }

        viewModelScope.launch {
            appAccountManager.accounts.combine(
                appAccountManager.selectedAccountFlow
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
                            UserSessionAndPerson(
                                session = UserSession(it, null),
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
                        val accountScope = appAccountManager.getOrCreateAccountScope(account)
                        val dataSource: SchoolDataSource = accountScope.get()
                        dataSource.personDataSource.findByGuidAsFlow(
                            account.userGuid
                        ).collect { person ->
                            _uiState.update { prev ->
                                prev.copy(
                                    accounts = prev.accounts.replaceOrAppend(
                                        UserSessionAndPerson(
                                            session = UserSession(account, null),
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

    fun onClickAccount(account: UserAccount) {
        appAccountManager.switchAccount(account)

        viewModelScope.launch {
            val accountScope = appAccountManager.getOrCreateAccountScope(account)
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
            appAccountManager.switchProfile(person.guid)
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
        uiState.value.selectedAccount?.also {
            viewModelScope.launch {
                appAccountManager.removeAccount(it.session.account)
            }
        }
    }

    fun onClickShareFeedback(){
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(ShareFeedback)
        )
    }
}