package org.openeel.shared.viewmodel.person.inviteperson
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.delay
import org.openeel.shared.domain.sharelink.LaunchSendEmailUseCase
import org.openeel.shared.domain.sharelink.LaunchShareLinkUseCase
import org.openeel.shared.domain.sharelink.LaunchSendSmsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.domain.GetWritableRolesListUseCase
import org.openeel.datalayer.school.ext.copyInvite
import org.openeel.datalayer.school.ext.isApprovalRequiredNow
import org.openeel.datalayer.school.ext.newUserInviteUid
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.ClassInviteModeEnum
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.NewUserInvite
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.libutil.ext.CHAR_POOL_NUMBERS
import org.openeel.libutil.ext.randomString
import org.openeel.libutil.util.time.systemTimeInMillis
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.clipboard.SetClipboardStringUseCase
import org.openeel.shared.domain.createlink.CreateInviteLinkUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_shared_school_device
import org.openeel.shared.generated.resources.invitation
import org.openeel.shared.generated.resources.invite_person
import org.openeel.shared.navigation.InvitePerson
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.AppBarSearchUiState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * @property approvalRequired is true when the time is after the approval required time on the invite
 *           This cannot be handled without a backing property, because we need to update it after
 *           the time runs out, even though the invite itself hasn't changed.
 */
data class InvitePersonUiState(
    val inviteOptions: InvitePerson.NewUserInviteOptions = InvitePerson.NewUserInviteOptions(null),
    val invite: DataLoadState<Invite2> = DataLoadingState(),
    val approvalRequired: Boolean = true,
    val inviteUrl: Url? = null,
    val selectedRole: PersonRoleEnum? = null,
    val className: String? = null,
    val schoolName: UiText? = null,
    val roleOptions: List<PersonRoleEnum> = emptyList(),
    val isSharedDeviceMode: Boolean = false
) {
    val inviteCode: String?
        get() = invite.dataOrNull()?.code

    val showRoleSelection: Boolean
        get() = invite.dataOrNull() is NewUserInvite

    val showClassInviteMode: Boolean
        get() = (invite.dataOrNull() as? ClassInvite)?.role == EnrollmentRoleEnum.STUDENT

}

class InvitePersonViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val setClipboardStringUseCase: SetClipboardStringUseCase,
    private val smsLinkLauncher: LaunchSendSmsUseCase,
    private val shareLinkLauncher: LaunchShareLinkUseCase,
    private val launchSendEmailUseCase: LaunchSendEmailUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: InvitePerson = savedStateHandle.toRoute()

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val createInviteLinkUseCase: CreateInviteLinkUseCase by inject()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(InvitePersonUiState())

    val uiState = _uiState.asStateFlow()

    /**
     * This ViewModel is a little different to the "normal" case. When the user selects a new user
     * role from the dropdown, this changes the uid of the invite that we want to actually show
     *
     * Hence the inviteUid is modelled as a flow, which is then collected.
     */
    private val _inviteUid = MutableStateFlow<String?>(null)

    private val getWritableRolesListUseCase: GetWritableRolesListUseCase by inject()

    init {
        val isSharedDeviceMode = when (val options = route.invitePersonOptions) {
            is InvitePerson.NewUserInviteOptions -> {
                options.presetRole == PersonRoleEnum.SHARED_SCHOOL_DEVICE
            }
            else -> false
        }
        _uiState.update { it.copy(isSharedDeviceMode = isSharedDeviceMode) }
        _appUiState.update {
            it.copy(
                title = if (isSharedDeviceMode) {
                    Res.string.add_shared_school_device.asUiText()
                } else {
                    Res.string.invite_person.asUiText()
                },
                searchState = AppBarSearchUiState(visible = false),
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }

        viewModelScope.launch {
            val currentPersonRole = accountManager.selectedAccountAndPersonFlow.first()
                ?.person?.roles?.first()?.roleEnum ?: return@launch

            val writableRoles = getWritableRolesListUseCase(currentPersonRole)
            val selectedRole = if (!isSharedDeviceMode) {
                writableRoles.firstOrNull() ?: PersonRoleEnum.STUDENT
            } else {
                PersonRoleEnum.SHARED_SCHOOL_DEVICE
            }
            _uiState.update {
                it.copy(
                    roleOptions =  writableRoles,
                    selectedRole = selectedRole,
                    schoolName = accountManager.activeAccount?.school?.name?.asUiText()
                )
            }

            _inviteUid.value = (route.invitePersonOptions as? InvitePerson.ClassInviteOptions)?.inviteUid
                ?: selectedRole.newUserInviteUid

            _inviteUid.collectLatest { inviteUid ->
                if(inviteUid != null) {
                    schoolDataSource.inviteDataSource.findByUidAsFlow(
                        uid = inviteUid,
                        loadParams = DataLoadParams()
                    ).collectLatest { invite ->
                        _uiState.update { prev ->
                            prev.copy(
                                invite = invite,
                                approvalRequired = invite.dataOrNull()?.isApprovalRequiredNow() ?: true,
                                inviteUrl = invite.dataOrNull()?.let {
                                    createInviteLinkUseCase(it.code)
                                },
                            )
                        }

                        invite.dataOrNull()?.approvalRequiredAfter?.also { approvalRequiredInstant ->
                            val timeUntilApprovalRequired =
                                approvalRequiredInstant.toEpochMilliseconds() - systemTimeInMillis()

                            if(timeUntilApprovalRequired > 0) {
                                delay(timeUntilApprovalRequired)
                                _uiState.update { prev ->
                                    prev.copy(
                                        approvalRequired = true,
                                    )
                                }
                            }
                        }
                    }
                }else {
                    _uiState.update { it.copy(invite = NoDataLoadedState.notFound()) }
                }
            }
        }
    }

    private fun launchUpdateInvite(invite: Invite2) {
        launchWithLoadingIndicator {
            schoolDataSource.inviteDataSource.store(listOf(invite))
        }
    }


    fun copyInviteLinkToClipboard() {
        _uiState.value.invite.dataOrNull()?.code?.also { code ->
            setClipboardStringUseCase(createInviteLinkUseCase(code).toString())
        }
    }

    fun onApprovalEnabledChanged(enabled: Boolean) {
        val currentInvite = _uiState.value.invite.dataOrNull() ?: return
        val now = Clock.System.now()
        launchUpdateInvite(
            currentInvite.copyInvite(
                approvalRequiredAfter = if(!enabled) {
                    now + Invite2.APPROVAL_NOT_REQUIRED_INTERVAL_MINS.minutes
                }else {
                    now
                },
                lastModified = now
            )
        )
    }


    fun onSetClassInviteMode(mode: ClassInviteModeEnum) {
        val currentClassInvite = _uiState.value.invite.dataOrNull() as? ClassInvite ?: return

        _inviteUid.update {
            ClassInvite.uidFor(
                classUid = currentClassInvite.classUid,
                role = currentClassInvite.role,
                inviteMode = mode,
            )
        }
    }

    fun onRoleChange(role: PersonRoleEnum) {
        _inviteUid.update { role.newUserInviteUid }
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun onSendLinkViaSms() {
        viewModelScope.launch {
            _uiState.value.invite.dataOrNull()?.code?.also {
                smsLinkLauncher(createInviteLinkUseCase(it).toString())
            }
        }
    }

    fun onSendLinkViaEmail() {
        viewModelScope.launch {
            _uiState.value.invite.dataOrNull()?.code?.also { code ->
                launchSendEmailUseCase(
                    LaunchSendEmailUseCase.LaunchSendEmailRequest(
                        subject = getString(Res.string.invitation),
                        body = createInviteLinkUseCase(code).toString(),
                        to = null,
                    )
                )
            }
        }
    }

    fun onShareLink() {
        viewModelScope.launch {
            _uiState.value.invite.dataOrNull()?.code?.also { code ->
                shareLinkLauncher(
                    body = createInviteLinkUseCase(code).toString()
                )
            }
        }
    }

    fun onClickResetCode() {
        _uiState.value.invite.dataOrNull()?.also { currentInvite ->
            launchUpdateInvite(
                currentInvite.copyInvite(
                    code = randomString(10, CHAR_POOL_NUMBERS),
                    lastModified = Clock.System.now(),
                )
            )
        }
    }

    fun onClickInviteCode() {
        _uiState.value.invite.dataOrNull()?.also {
            setClipboardStringUseCase(it.code)
        }
    }

}