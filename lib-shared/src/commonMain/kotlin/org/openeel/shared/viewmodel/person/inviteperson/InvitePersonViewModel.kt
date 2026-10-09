package world.respect.shared.viewmodel.person.inviteperson
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.delay
import world.respect.shared.domain.sharelink.LaunchSendEmailUseCase
import world.respect.shared.domain.sharelink.LaunchShareLinkUseCase
import world.respect.shared.domain.sharelink.LaunchSendSmsUseCase
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
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.domain.GetWritableRolesListUseCase
import world.respect.datalayer.school.ext.copyInvite
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.ext.newUserInviteUid
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.libutil.ext.CHAR_POOL_NUMBERS
import world.respect.libutil.ext.randomString
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_shared_school_device
import world.respect.shared.generated.resources.invitation
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState
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