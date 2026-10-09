package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.credentials.passkey.RespectUserHandle
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.ext.accepterEnrollmentRole
import world.respect.datalayer.school.ext.accepterPersonRole
import world.respect.datalayer.school.ext.copyWithInviteInfo
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.model.StatusEnum
import world.respect.libutil.ext.randomString
import world.respect.lib.dataloadstate.throwable.withHttpStatus
import world.respect.shared.domain.account.AuthResponse
import world.respect.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import world.respect.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.di.SchoolDataSourceLocalProvider
import world.respect.shared.util.toPerson
import kotlin.time.Clock

/**
 * Server-side use case that handles redeeming an invite: that is when a (new) user client signing up
 * provies both a) invite info and code and b) information about the account they want to create (
 * name, gender, username, password/passkey, etc).
 */
class RedeemInviteUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolUrl: Url,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val getTokenAndUserProfileUseCase: GetTokenAndUserProfileWithCredentialUseCase,
    private val schoolDataSource: SchoolDataSourceLocalProvider,
    private val json: Json,
    private val getPasskeyProviderInfoUseCase: GetPasskeyProviderInfoUseCase,
    private val encryptPersonPasswordUseCase: EncryptPersonPasswordUseCase,
    private val checkUsernameUniqueUseCase: CheckUsernameUniqueUseCase,
) : RedeemInviteUseCase, KoinComponent {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        useActiveUserAuth: Boolean
    ): AuthResponse {
        val inviteFromDb = schoolDb.getInviteEntityDao().getInviteByInviteCode(
            redeemRequest.code
        )?.toModel()
            ?: throw IllegalArgumentException("invite not found for code: ${redeemRequest.code}")
                .withHttpStatus(404)

        val accountGuid = redeemRequest.account.guid

        val isSharedDeviceInvite =
            redeemRequest.invite.accepterPersonRole == PersonRoleEnum.SHARED_SCHOOL_DEVICE
        val approvalRequired = if (isSharedDeviceInvite && useActiveUserAuth) {
            false
        } else {
            inviteFromDb.isApprovalRequiredNow()
        }
        val accountPerson = redeemRequest.accountPersonInfo.toPerson(
            role = redeemRequest.invite.accepterPersonRole,
            username = redeemRequest.account.username,
            guid = accountGuid,
        ).copy(
            status = if(approvalRequired){
                PersonStatusEnum.PENDING_APPROVAL
            }else {
                PersonStatusEnum.ACTIVE
            },
        ).let {
            if(approvalRequired) {
                it.copyWithInviteInfo(
                    invite = redeemRequest.invite,
                    deviceInfo = redeemRequest.deviceInfo
                )
            }else {
                it
            }
        }

        val authenticatedPrincipleId = AuthenticatedUserPrincipalId(accountGuid)
        val schoolDataSourceVal = schoolDataSource(
            schoolUrl = schoolUrl, user = authenticatedPrincipleId,
        )

        if(accountPerson.username?.let { checkUsernameUniqueUseCase(it) } != true) {
            throw IllegalArgumentException("Username not unique anymore")
                .withHttpStatus(400)
        }

        schoolDataSourceVal.personDataSource.updateLocal(listOf(accountPerson))

        val enrollmentRole = inviteFromDb.accepterEnrollmentRole(approvalRequired)
        if(enrollmentRole != null && inviteFromDb is ClassInvite
                && inviteFromDb.inviteMode != ClassInviteModeEnum.VIA_PARENT
        ) {
            schoolDataSourceVal.enrollmentDataSource.updateLocal(
                listOf(
                    Enrollment(
                        uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                            Enrollment.TABLE_ID
                        ).toString(),
                        classUid = inviteFromDb.classUid,
                        personUid = accountPerson.guid,
                        role = enrollmentRole,
                        beginDate = Clock.System.now().toLocalDateTime(
                            TimeZone.currentSystemDefault()
                        ).date
                    )
                )
            )

            if(enrollmentRole == EnrollmentRoleEnum.STUDENT) {
                val updateXapiGroupUseCase = UpdateClazzStudentXapiGroupUseCase(
                    schoolDataSource = schoolDataSourceVal,
                    authenticatedUserPrincipalId = authenticatedPrincipleId,
                    schoolUrl = schoolUrl,
                )
                updateXapiGroupUseCase(inviteFromDb.classUid)
            }
        }

        val credential = redeemRequest.account.credential

        val authResponse = when (credential) {
            is RespectPasswordCredential -> {
                schoolDataSourceVal.personPasswordDataSource.store(
                    listOf(
                        encryptPersonPasswordUseCase(
                            EncryptPersonPasswordUseCase.Request(
                                personGuid = accountGuid,
                                password = credential.password,
                            )
                        )
                    )
                )

                getTokenAndUserProfileUseCase(credential)
            }

            is RespectPasskeyCredential -> {
                val passkeyCreatedResult = CreatePasskeyUseCase.PasskeyCreatedResult(
                    respectUserHandle = RespectUserHandle(
                        personUidNum = uidNumberMapper(accountGuid),
                        schoolUrl = schoolUrl
                    ),
                    authenticationResponseJSON = credential.passkeyWebAuthNResponse,
                    passkeyProviderInfo = getPasskeyProviderInfoUseCase(
                        credential.passkeyWebAuthNResponse.response.authenticatorData
                    )
                )

                schoolDataSourceVal.personPasskeyDataSource.store(
                    listOf(
                        passkeyCreatedResult.toPersonPasskey(
                            json = json,
                            personGuid = accountPerson.guid,
                            deviceName = redeemRequest.deviceName ?: "Unknown device type",
                        )
                    )
                )

                val token = AuthToken(
                    accessToken = randomString(32),
                    timeCreated = System.currentTimeMillis(),
                    ttl = GetTokenAndUserProfileWithCredentialDbImpl.TOKEN_DEFAULT_TTL,
                )

                val personGuidHash = uidNumberMapper(accountPerson.guid)
                schoolDb.getAuthTokenEntityDao().insert(
                    token.toEntity(
                        pGuid = accountPerson.guid,
                        pGuidHash = personGuidHash,
                        deviceInfo = redeemRequest.deviceInfo,
                    )
                )

                AuthResponse(
                    token = token,
                    person = accountPerson,
                )
            }

            is RespectQRBadgeCredential -> {
                throw IllegalArgumentException("Using a QR code badge to redeem invite for new account not yet supported")
            }
            null -> {
                // Handle shared school device case - no credential needed
                // For shared devices, we just create the person account without authentication credentials
                val token = AuthToken(
                    accessToken = randomString(32),
                    timeCreated = System.currentTimeMillis(),
                    ttl = GetTokenAndUserProfileWithCredentialDbImpl.TOKEN_DEFAULT_TTL,
                )

                val personGuidHash = uidNumberMapper(accountPerson.guid)
                schoolDb.getAuthTokenEntityDao().insert(
                    token.toEntity(
                        pGuid = accountPerson.guid,
                        pGuidHash = personGuidHash,
                        deviceInfo = redeemRequest.deviceInfo,
                    )
                )

                AuthResponse(
                    token = token,
                    person = accountPerson,
                )
            }
        }
        markFirstUserInviteAsDeleted(inviteFromDb, schoolDataSourceVal)

        return authResponse
    }
    /**
     * Deletes the invite if it's a first user invite (firstUser = true)
     * This ensures the first user invite can never be used again after the first user signs up
     */
    private suspend fun markFirstUserInviteAsDeleted(
        redeemedInvite: Invite2,
        schoolDataSourceVal: SchoolDataSourceLocal
    ) {
        // Check if this is a NewUserInvite with firstUser = true
        if (redeemedInvite is NewUserInvite && redeemedInvite.firstUser) {

            val deletedInvite = redeemedInvite.copy(
                status = StatusEnum.TO_BE_DELETED,
                lastModified = Clock.System.now()
            )

            schoolDataSourceVal.inviteDataSource.store(listOf(deletedInvite))
        }
    }
}