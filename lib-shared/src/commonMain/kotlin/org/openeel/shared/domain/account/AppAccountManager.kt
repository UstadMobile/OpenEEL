package org.openeel.shared.domain.account

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.scope.Scope
import org.openeel.credentials.passkey.OpenEelCredential
import org.openeel.shared.domain.account.invite.RedeemInviteRequest
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.shared.params.GetListCommonParams
import org.openeel.datalayer.school.SchoolPermissionGrantDataSource
import org.openeel.datalayer.school.model.Person
import org.openeel.libutil.util.putDebugCrashCustomData
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import org.openeel.shared.domain.account.invite.RedeemInviteUseCase
import org.openeel.shared.domain.school.MakeSchoolPathDirUseCase
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import org.openeel.shared.util.ext.isSameAccount

/**
 *
 * @property accounts all the accounts that the user has signed in. The user can use the account
 *           switcher to switch between them.
 */
class AppAccountManager(
    private val settings: Settings,
    private val json: Json,
    private val tokenManager: TokenManager,
    private val appDataSource: SchoolDirectoryDataSource,
): KoinComponent {

    private val _storedAccounts = MutableStateFlow<List<UserAccount>>(
        value = settings.getStringOrNull(SETTINGS_KEY_STORED_ACCOUNTS)?.let {
            json.decodeFromString(it)
        } ?: emptyList()
    )

    val accounts = _storedAccounts.asStateFlow()

    private val _activeSession = MutableStateFlow(
        settings.getStringOrNull(SETTINGS_KEY_ACTIVE_SESSION)?.let {
            json.decodeFromString(UserSession.serializer(), it)
        }
    )

    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())

    /**
     * The active account is the stored account that the user has currently
     * selected (eg the one normally displayed in the top right). The active account is null
     * if the user is not currently signed in.
     */
    val activeAccount: UserAccount?
        get() {
            val activeSessionVal = _activeSession.value ?: return null
            return _storedAccounts.value.firstOrNull {
                it.isSameAccount(activeSessionVal.account)
            }
        }

    val selectedAccountFlow: Flow<UserAccount?> = _storedAccounts.combine(
        _activeSession
    ) { accountList, activeSession ->
        if(activeSession == null) {
            null
        } else {
            accountList.firstOrNull { activeSession.account.isSameAccount(it) }
        }
    }

    val selectedAccountAndPersonFlow: Flow<UserSessionAndPerson?> =channelFlow {
        _activeSession.collectLatest { session ->
            if(session != null) {
                val accountScope = getOrCreateAccountScope(session.account)
                val schoolDataSource: SchoolDataSource = accountScope.get()
                val activePersonUid = (session.profilePersonUid ?: session.account.userGuid)

                schoolDataSource.personDataSource.listAsFlow(
                    loadParams = DataLoadParams(),
                    params = PersonDataSource.GetListParams(
                        common = GetListCommonParams(
                            guid = session.account.userGuid
                        ),
                        includeRelated = true,
                    )
                ).collect { persons ->
                    val personsVal = persons.dataOrNull()
                    send(
                        element = personsVal?.firstOrNull {
                            it.guid == activePersonUid
                        }?.let { activeProfilePerson ->
                            UserSessionAndPerson(
                                session = session,
                                person = activeProfilePerson,
                                relatedPersons = personsVal.filterNot { it.guid == activePersonUid },
                            )
                        }
                    )
                }
            }else {
                send(null)
            }
        }
    }.shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)

    init {
        putDebugCrashCustomData("SelectedAccount", activeAccount.toString())
    }

    /**
     * Login a user with the given credentials
     */
    suspend fun login(
        credential: OpenEelCredential,
        schoolUrl: Url,
    ) : AuthResponse {
        val schoolScopeId = SchoolDirectoryEntryScopeId(schoolUrl, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            schoolScopeId.scopeId
        )

        val authUseCase: GetTokenAndUserProfileWithCredentialUseCase = schoolScope.get()
        val authResponse = authUseCase(credential)

        val schoolDirectoryEntry = appDataSource.schoolDirectoryEntryResource
            .getSchoolDirectoryEntryByUrl(schoolUrl)
            .dataOrNull() ?: throw IllegalStateException()

        val userAccount = UserAccount(
            userGuid = authResponse.person.guid,
            school = schoolDirectoryEntry,
        )

        initSession(authResponse, UserSession(userAccount, null))

        return authResponse
    }

    @Suppress("unused")
    suspend fun register(
        redeemInviteRequest: RedeemInviteRequest,
        schoolUrl: Url,
    ): Person {
        val schoolScopeId = SchoolDirectoryEntryScopeId(
            schoolUrl, null,
        )
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            schoolScopeId.scopeId
        )

        val redeemInviteUseCase: RedeemInviteUseCase = schoolScope.get()
        val authResponse = redeemInviteUseCase(redeemInviteRequest)

        val schoolDirectoryEntry = appDataSource.schoolDirectoryEntryResource.getSchoolDirectoryEntryByUrl(
            schoolUrl
        ).dataOrNull() ?: throw IllegalStateException()

        initSession(
            authResponse = authResponse,
            session = UserSession(
                account = UserAccount(
                    authResponse.person.guid, schoolDirectoryEntry
                ),
                profilePersonUid = null,
            )
        )

        return authResponse.person
    }

    private suspend fun initSession(
        authResponse: AuthResponse,
        session: UserSession,
    ) {
        val schoolScope: Scope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(
                schoolUrl = session.account.school.self, accountPrincipalId = null,
            ).scopeId
        )
        tokenManager.storeToken(session.account.scopeId, authResponse.token)

        val accountScope = getOrCreateAccountScope(session.account)

        val schoolDataSource: SchoolDataSource = accountScope.get()

        //Ensure the active user is loaded into the database
        schoolDataSource.personDataSource.list(
            loadParams = DataLoadParams(),
            params = PersonDataSource.GetListParams(
                common = GetListCommonParams(
                    guid = authResponse.person.guid
                ),
                includeRelated = true,
            )
        )

        //Load school permission grants (rules)
        schoolDataSource.schoolPermissionGrantDataSource.list(
            DataLoadParams(), SchoolPermissionGrantDataSource.GetListParams()
        ).dataOrNull() ?: throw IllegalStateException("Could not load permission grants")

        //now we can get the datalayer by creating a UserAccount scope
        val mkDirUseCase: MakeSchoolPathDirUseCase? = schoolScope.getOrNull()
        mkDirUseCase?.invoke()

        _activeSession.value = session

        if(!_storedAccounts.value.any { it.isSameAccount(session.account) }) {
            val newValue = _storedAccounts.updateAndGet { prev ->
                listOf(session.account) + prev
            }

            settings[SETTINGS_KEY_STORED_ACCOUNTS] = json.encodeToString(newValue)
        }

        settings[SETTINGS_KEY_ACTIVE_SESSION] = json.encodeToString(session)

        putDebugCrashCustomData("SelectedAccount", activeAccount.toString())
    }


    /**
     * Remove the given account from the list of stored accounts. If the given account is also the
     * used for the active session, the session is ended and the active session will be null (e.g.
     * when the user logs out).
     */
    @Suppress("RedundantSuspendModifier") //Likely needs to be suspended to communicate to server
    suspend fun removeAccount(account: UserAccount) {
        _activeSession.update { prev ->
            if(prev?.account?.isSameAccount(account) == true )
                null
            else
                prev
        }

        val accountScope = getOrCreateAccountScope(account)
        accountScope.close()

        tokenManager.removeToken(account.scopeId)

        val storedAccountsToCommit = _storedAccounts.updateAndGet { prev ->
            prev.filterNot {
                it.isSameAccount(account)
            }
        }

        settings[SETTINGS_KEY_STORED_ACCOUNTS] = json.encodeToString(storedAccountsToCommit)

        val accountsOnRealmScope = accounts.value.count {
            it.school.self == account.school.self
        }

        if(accountsOnRealmScope == 0) {
            //close it
            val schoolScopeId = SchoolDirectoryEntryScopeId(
                schoolUrl = account.school.self, accountPrincipalId = null
            )
            val schoolScope = getKoin().getScopeOrNull(schoolScopeId.scopeId)
            schoolScope?.close()
        }

    }

    fun switchAccount(account: UserAccount) {
        if(!_storedAccounts.value.any { it.isSameAccount(account) }) {
            throw IllegalArgumentException("switchAccount: account not stored/available")
        }

        _activeSession.value = UserSession(account, null)
    }

    /**
     * When the RespectAccount scope is created it MUST be linked to the parent school scope.
     */
    fun getOrCreateAccountScope(account: UserAccount): Scope {
        return getKoin().getOrCreateScope<UserAccount>(account.scopeId)
    }

    fun requireActiveAccountScope(): Scope {
        return activeAccount?.let { getOrCreateAccountScope(it) }
            ?: throw IllegalStateException("require scope for selected account: no account selected")
    }

    fun requireActiveSchoolUrl(): Url {
        return activeAccount?.school?.self
            ?: throw IllegalStateException("require school url for active account: no account selected")
    }

    suspend fun switchProfile(personUid: String) {
        val currentSession = _activeSession.value
            ?: throw IllegalStateException("switchProfile: no active session")

        if(!_storedAccounts.value.any { it.isSameAccount(currentSession.account) }) {
            throw IllegalArgumentException("switchProfile: account not stored/available")
        }

        val accountScope = getOrCreateAccountScope(currentSession.account)
        val schoolDataSource: SchoolDataSource = accountScope.get()
        schoolDataSource.personDataSource.findByGuid(
            DataLoadParams(),
            personUid
        )


        val newSession = currentSession.copy(profilePersonUid = personUid)
        _activeSession.value = newSession
        settings[SETTINGS_KEY_ACTIVE_SESSION] = json.encodeToString(newSession)

        putDebugCrashCustomData("SelectedAccount", activeAccount.toString())
    }


    companion object {

        const val SETTINGS_KEY_STORED_ACCOUNTS = "accountmanager-storedaccounts"

        const val SETTINGS_KEY_ACTIVE_SESSION = "accountmanager-activesession"
    }
}