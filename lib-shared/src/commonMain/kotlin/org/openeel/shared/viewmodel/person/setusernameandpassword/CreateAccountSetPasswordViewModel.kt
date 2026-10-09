package world.respect.shared.viewmodel.person.setusernameandpassword

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.validatepassword.ValidatePasswordUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.set_password
import world.respect.shared.navigation.CreateAccountSetPassword
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import kotlin.time.Clock

data class CreateAccountSetPasswordUiState(
    val password: String = "",
    val passwordErr: UiText? = null,
)

class CreateAccountSetPasswordViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: CreateAccountSetPassword = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(CreateAccountSetPasswordUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.set_password.asUiText(),
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    text = Res.string.save.asUiText(),
                    visible = true,
                    onClick = ::onClickSave
                )
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onClickSave() {
        try {
            validatePasswordUseCase(uiState.value.password)
        } catch (t: Throwable) {
            _uiState.update { it.copy(passwordErr = t.getUiTextOrGeneric()) }
            return
        }

        launchWithLoadingIndicator {
            try {
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

                route.username?.let { username ->
                    val person = schoolDataSource.personDataSource.findByGuid(
                        DataLoadParams(), route.guid
                    ).dataOrNull() ?: throw IllegalStateException("Person not found")

                    schoolDataSource.personDataSource.store(
                        listOf(
                            person.copy(
                                username = username,
                                lastModified = Clock.System.now(),
                            )
                        )
                    )
                }

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        ManageAccount(guid = route.guid),
                        popUpToClass = PersonDetail::class,
                        popUpToInclusive = true,
                    )
                )

            } catch (e: Throwable) {
                Napier.e("Error saving password and username", e)
            }
        }
    }
}