package org.openeel.shared.viewmodel.scanqrcode

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.openeel.credentials.passkey.RespectQRBadgeCredential
import org.openeel.libutil.ext.schoolUrlOrNull
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import org.openeel.shared.ext.NextAfterScan
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.more_options
import org.openeel.shared.generated.resources.paste_url
import org.openeel.shared.generated.resources.qr_code_invalid_format
import org.openeel.shared.generated.resources.scan_qr_code
import org.openeel.shared.navigation.CreateAccountSetUsername
import org.openeel.shared.navigation.Home
import org.openeel.shared.navigation.ManageAccount
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.ScanQRCode
import org.openeel.shared.navigation.sendResultIfResultExpected
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.exception.getUiTextOrGeneric
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.AppActionButton
import org.openeel.shared.viewmodel.app.appstate.AppStateIcon

data class ScanQRCodeUiState(
    val errorMessage: UiText? = null,
    val showManualEntryDialog: Boolean = false,
)

class ScanQRCodeViewModel(
    savedStateHandle: SavedStateHandle,
    private val resultReturner: NavResultReturner,
    private val respectAccountManager: RespectAccountManager,
    private val resolveUrlToNavCommandUseCase: ResolveUrlToNavCommandUseCase,
) : RespectViewModel(savedStateHandle), KoinComponent {

    private val _uiState = MutableStateFlow(ScanQRCodeUiState())

    val uiState: Flow<ScanQRCodeUiState> = _uiState.asStateFlow()

    private val route: ScanQRCode = savedStateHandle.toRoute()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.scan_qr_code.asUiText(),
                navigationVisible = true,
                hideBottomNavigation = true,
                actions = listOf(
                    AppActionButton(
                        icon = AppStateIcon.MORE_VERT,
                        contentDescription = Res.string.more_options.asUiText(),
                        text = Res.string.paste_url.asUiText(),
                        onClick = {
                            _uiState.update { currentState ->
                                currentState.copy(showManualEntryDialog = true)
                            }
                        },
                        id = "more_options_qr_scan",
                        display = AppActionButton.Companion.ActionButtonDisplay.OVERFLOW_MENU
                    )
                ),
                userAccountIconVisible = false
            )
        }
    }

    fun onQrCodeScanned(url: String) {
        _uiState.update {
            it.copy(errorMessage = null, showManualEntryDialog = false)
        }

        launchWithLoadingIndicator(
            onShowError = { error ->
                _uiState.update { it.copy(errorMessage = error) }
            }
        ) {
            val urlObj = Url(url)

            val navCommandForUrl = resolveUrlToNavCommandUseCase(urlObj)

            when {
                //If a result was requested to be returned via NavResultReturner, then do that
                resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = url,
                ) -> {
                    //Nothing more to do, a result was requested and sent.
                }

                //If user needs to go forward to ManageAccount as part of assigning a QR badge
                // to an existing user, do that.
                route.nextAfterScan == NextAfterScan.GoToManageAccount -> {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            destination = ManageAccount(
                                guid = route.guid ?: throw IllegalArgumentException(),
                                setPersonQrBadgeUrlStr = url,
                                setPersonQrBadgeUsername = route.username
                            ),
                            popUpToClass = CreateAccountSetUsername::class,
                            popUpToInclusive = true
                        )
                    )
                }

                navCommandForUrl != null -> {
                    _navCommandFlow.tryEmit(navCommandForUrl)
                }

                //Else navigate based on the QR code. Right now, this is only authentication using
                //QR Badge, could also be an invite, playlist link, etc.
                else -> {
                    authenticateWithQrCode(urlObj)
                }
            }
        }
    }

    fun hideManualEntryDialog() {
        _uiState.update { currentState ->
            currentState.copy(showManualEntryDialog = false)
        }
    }

    fun onQrCodeScanError(exception: Exception) {
        _uiState.update {
            it.copy(
                errorMessage = exception.getUiTextOrGeneric(),
            )
        }
    }

    private suspend fun authenticateWithQrCode(url: Url) {
        val credential = RespectQRBadgeCredential(qrCodeUrl = url)
        val schoolUrl = url.schoolUrlOrNull()

        if (schoolUrl == null) {
            _uiState.update {
                it.copy(
                    errorMessage = Res.string.qr_code_invalid_format.asUiText(),
                    showManualEntryDialog = false,
                )
            }

            return
        }

        respectAccountManager.login(
            credential = credential,
            schoolUrl = schoolUrl
        )

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = Home,
                clearBackStack = true,
            )
        )
    }

    fun onClickTryAgain() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }
}