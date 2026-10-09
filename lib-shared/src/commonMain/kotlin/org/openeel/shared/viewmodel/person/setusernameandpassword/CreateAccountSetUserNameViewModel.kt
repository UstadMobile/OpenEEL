package world.respect.shared.viewmodel.person.setusernameandpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.username.UsernameSuggestionUseCase
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.domain.account.validatepassword.ValidatePasswordUseCase
import world.respect.shared.ext.NextAfterScan
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.username_already_taken
import world.respect.shared.navigation.CreateAccountSetPassword
import world.respect.shared.navigation.CreateAccountSetUsername
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.time.Clock

data class CreateAccountSetUserNameUiState(
    val username: String = "",
    val usernameErr: UiText? = null,
    val passwordErr: UiText? = null,
    val password: String = "",
    val isPasswordSet: Boolean = false,
    val isQrBadgeSet: Boolean = false,
    val showQrBadgeInfoBox: Boolean = false
)

/**
 * Used to manually set a username and password for an existing person where the active user (eg.
 * admin) has sufficient permission to do so.
 */
class CreateAccountSetUserNameViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: CreateAccountSetUsername = savedStateHandle.toRoute()

    private val usernameSuggestionUseCase: UsernameSuggestionUseCase by inject()

    private val checkUsernameUniqueUseCase: CheckUsernameUniqueUseCase by inject()

    private val _uiState = MutableStateFlow(CreateAccountSetUserNameUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.create_account.asUiText(),
                hideBottomNavigation = true,
            )
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.findByGuidAsFlow(
                route.guid
            ).collectLatest { personResult ->
                val person = personResult.dataOrNull()
                val isStudent = person?.isStudent() == true


                val currentUsername = _uiState.value.username

                val suggestedUsername = person?.fullName()?.takeIf { currentUsername.isEmpty() }?.let {
                    try {
                        usernameSuggestionUseCase(it)
                    }catch(e: Throwable) {
                        Napier.w("Could not get suggested username", e)
                        null
                    }
                }

                _uiState.update { prev ->
                    prev.copy(
                        showQrBadgeInfoBox = isStudent,
                        username = prev.username.ifEmpty {
                            suggestedUsername ?: ""
                        }
                    )
                }

                _appUiState.update { appUiState ->
                    appUiState.copy(
                        actionBarButtonState = if(!isStudent) {
                            ActionBarButtonUiState(
                                text = Res.string.save.asUiText(),
                                visible = true,
                                onClick = ::onClickSave
                            )
                        }else {
                            ActionBarButtonUiState(visible = false)
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.personQrBadgeDataSource.findByGuidAsFlow(
                DataLoadParams(), route.guid
            ).collect {
                _uiState.update { prev ->
                    prev.copy(
                        isQrBadgeSet = it.dataOrNull() != null
                    )
                }
            }
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = filterUsernameUseCase(username, "")) }
    }

    private suspend fun checkUsernameOk(username: String): String? {
        val usernameVal = username.trim()
        val usernameValidation = validateUsernameUseCase(usernameVal)
        _uiState.update {
            it.copy(
                usernameErr = usernameValidation.errorMessage?.asUiText()
            )
        }

        if (usernameValidation.errorMessage != null)
            return null

        return try {
            if(checkUsernameUniqueUseCase(usernameVal)) {
                usernameVal
            }else {
                _uiState.update {
                    it.copy(usernameErr = Res.string.username_already_taken.asUiText())
                }

                null
            }
        }catch(e: Throwable) {
            Napier.w("Something wrong setting username", e)
            _uiState.update { it.copy(usernameErr = e.getUiTextOrGeneric()) }
            null
        }
    }

    fun onClickSave() {
        launchWithLoadingIndicator {
            try {
                val username = checkUsernameOk(uiState.value.username)
                    ?: return@launchWithLoadingIndicator

                try {
                    validatePasswordUseCase(uiState.value.password)
                }catch(e: Throwable) {
                    _uiState.update { it.copy(passwordErr = e.message?.asUiText()) }
                    return@launchWithLoadingIndicator
                }

                val person = schoolDataSource.personDataSource.findByGuid(
                    DataLoadParams(), route.guid
                ).dataOrNull() ?: throw IllegalStateException()

                schoolDataSource.personDataSource.store(
                    listOf(
                        person.copy(
                            username = username,
                            lastModified = Clock.System.now(),
                        )
                    )
                )

                schoolDataSource.personPasswordDataSource.store(
                    listOf(
                        encryptPersonPasswordUseCase(
                            EncryptPersonPasswordUseCase.Request(
                                personGuid = route.guid,
                                password = uiState.value.password
                            )
                        )
                    )
                )

                _navCommandFlow.tryEmit(NavCommand.PopUp())
            } catch (e: Throwable) {
                Napier.e("Error saving username and password", e)
            }
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onClickAssignQrCodeBadge() {
        launchWithLoadingIndicator {
            val username = uiState.value.username.trim()
            val usernameValidation = validateUsernameUseCase(username)
            _uiState.update {
                it.copy(
                    usernameErr = usernameValidation.errorMessage?.asUiText()
                )
            }
            val schoolUrl = accountManager.activeAccount?.school?.self

            if (usernameValidation.errorMessage == null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        ScanQRCode.create(
                            guid = route.guid,
                            username = username,
                            schoolUrl = schoolUrl,
                            nextAfterScan = NextAfterScan.GoToManageAccount
                        )
                    )
                )
            }
        }
    }

    fun onClickSetPassword() {
        launchWithLoadingIndicator {
            val username = checkUsernameOk(uiState.value.username)
                ?: return@launchWithLoadingIndicator

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    CreateAccountSetPassword(
                        guid = route.guid,
                        username = username
                    ),
                )
            )
        }
    }

    fun onClickQrBadgeLearnMore() {
        //TODO
    }
}