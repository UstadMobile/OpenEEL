package world.respect.shared.viewmodel.person.changepassword

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
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.validatepassword.ValidatePasswordUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.change_password
import world.respect.shared.generated.resources.invalid_password
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.saved
import world.respect.shared.navigation.ChangePassword
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.datalayer.db.school.ext.canAdminAccountFor
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher

data class ChangePasswordUiState(
    val requireOldPassword: Boolean = true,
    val oldPassword: String = "",
    val oldPasswordError: UiText? = null,
    val newPassword: String = "",
    val newPasswordError: UiText? = null,
    val generalError: UiText? = null,
)

class ChangePasswordViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource : SchoolDataSource by inject()

    private val authenticatePasswordUseCase: AuthenticatePasswordUseCase by inject()

    private val _uiState = MutableStateFlow(ChangePasswordUiState())

    val uiState = _uiState.asStateFlow()

    private val route: ChangePassword = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.change_password.asUiText(),
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave,
                )
            )
        }

        viewModelScope.launch {
            val selectedAcctFlow = accountManager.selectedAccountAndPersonFlow
            val personFlow = schoolDataSource.personDataSource.findByGuidAsFlow(
                guid = route.guid
            )

            selectedAcctFlow.combine(personFlow) { selectedAcct, person ->
                Pair(selectedAcct, person)
            }.collect { (selectedAcct, person) ->
                val activeUserPersonVal = selectedAcct?.person
                val personVal = person.dataOrNull()

                _uiState.update {
                    it.copy(
                        requireOldPassword = if(activeUserPersonVal != null && personVal != null) {
                            !activeUserPersonVal.canAdminAccountFor(personVal)
                        }else {
                            true
                        }
                    )
                }
            }
        }
    }

    fun onClickSave() {
        launchWithLoadingIndicator {
            _uiState.update {
                it.copy(
                    oldPasswordError = null,
                    newPasswordError = null,
                    generalError = null,
                )
            }

            val person = schoolDataSource.personDataSource.findByGuid(
                DataLoadParams(),route.guid
            ).dataOrNull() ?: return@launchWithLoadingIndicator

            try {
                if(uiState.value.requireOldPassword) {
                    //check existing password
                    try {
                        val usernameVal = person.username ?: throw IllegalStateException()
                        authenticatePasswordUseCase(
                            RespectPasswordCredential(usernameVal, _uiState.value.oldPassword)
                        )
                    }catch(_: Throwable) {
                        _uiState.update {
                            it.copy(
                                oldPasswordError = Res.string.invalid_password.asUiText(),
                            )
                        }

                        return@launchWithLoadingIndicator
                    }
                }


                val newPassword = _uiState.value.newPassword.trim()

                try {
                    validatePasswordUseCase(newPassword)
                }catch(t: Throwable) {
                    _uiState.update { it.copy(newPasswordError = t.getUiTextOrGeneric()) }
                    return@launchWithLoadingIndicator
                }

                schoolDataSource.personPasswordDataSource.store(
                    listOf(
                        encryptPersonPasswordUseCase(
                            EncryptPersonPasswordUseCase.Request(
                                personGuid = person.guid,
                                password = newPassword,
                            )
                        )
                    )
                )

                snackBarDispatcher.showSnackBar(Snack(Res.string.saved.asUiText()))

                _navCommandFlow.tryEmit(
                    NavCommand.PopUp()
                )
            }catch(t: Throwable) {
                _uiState.update {
                    it.copy(
                        generalError = t.getUiTextOrGeneric(),
                    )
                }
            }
        }
    }


    fun onChangeOldPassword(oldPassword: String) {
        _uiState.update { it.copy(oldPassword = oldPassword, oldPasswordError = null) }
    }

    fun onChangeNewPassword(newPassword: String) {
        _uiState.update { it.copy(newPassword = newPassword, newPasswordError = null) }
    }

}