package org.openeel.shared.viewmodel.manageuser.acceptinvite


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import org.openeel.credentials.passkey.RespectPasswordCredential
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.respect.model.invite.RespectInviteInfo
import org.openeel.datalayer.school.ext.isChildUser
import org.openeel.datalayer.school.model.Person
import org.openeel.lib.opds.model.LangMap
import org.openeel.shared.domain.account.invite.GetInviteInfoUseCase
import org.openeel.shared.domain.account.invite.RespectRedeemInviteRequest
import org.openeel.shared.domain.account.invite.RespectRedeemInviteRequest.PersonInfo
import org.openeel.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import org.openeel.shared.domain.getdeviceinfo.toUserFriendlyString
import org.openeel.shared.domain.school.SchoolPrimaryKeyGenerator
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.invitation
import org.openeel.shared.generated.resources.something_wrong_with_invite
import org.openeel.shared.navigation.AcceptInvite
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.SignupScreen
import org.openeel.shared.navigation.TermsAndCondition
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel

data class AcceptInviteUiState(
    val inviteInfo: RespectInviteInfo? = null,
    val errorText: UiText? = null,
    val isTeacherInvite: Boolean = false,
    val schoolName: LangMap? = null,
    val schoolUrl: Url? = null,
) {
    val nextButtonEnabled: Boolean
        get() = inviteInfo?.invite != null

}

class AcceptInviteViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val schoolDirectoryDataSource: SchoolDirectoryDataSource,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: AcceptInvite = savedStateHandle.toRoute()

    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val getInviteInfoUseCase: GetInviteInfoUseCase = scope.get()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator = scope.get()

    private val _uiState = MutableStateFlow(
        AcceptInviteUiState(schoolUrl = route.schoolUrl)
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.invitation.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = route.canGoBack,
            )
        }

        launchWithLoadingIndicator(
            onShowError = {
                _uiState.update { it.copy(errorText = Res.string.something_wrong_with_invite.asUiText()) }
            }
        ) {
            val inviteInfo = getInviteInfoUseCase(route.code)

            _uiState.update {
                it.copy(
                    inviteInfo = inviteInfo,
                    isTeacherInvite = false
                )
            }
        }

        viewModelScope.launch {
            val schoolDirEntry = schoolDirectoryDataSource.schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
                route.schoolUrl
            ).dataOrNull() ?: return@launch

            _uiState.update {
                it.copy(schoolName = schoolDirEntry.name)
            }
        }
    }

    fun onClickNext() {
        val invite = uiState.value.inviteInfo?.invite ?: return

        val inviteRedeemRequest = RespectRedeemInviteRequest(
            code = invite.code,
            accountPersonInfo = PersonInfo(),
            account = RespectRedeemInviteRequest.Account(
                guid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Person.TABLE_ID).toString(),
                username = "",
                credential = RespectPasswordCredential(username = "", password = ""),
            ),
            deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
            deviceInfo = getDeviceInfoUseCase(),
            invite = invite
        )

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = if(!invite.isChildUser()) {
                    TermsAndCondition.create(
                        schoolUrl = route.schoolUrl,
                        inviteRequest = inviteRedeemRequest,
                    )
                }else {
                    SignupScreen.create(
                        schoolUrl = route.schoolUrl,
                        inviteRequest = inviteRedeemRequest,
                    )
                }
            )
        )
    }

}
