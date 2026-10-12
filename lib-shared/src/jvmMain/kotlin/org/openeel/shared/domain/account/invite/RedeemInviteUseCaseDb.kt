package org.openeel.shared.domain.account.invite

import io.ktor.http.Url
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.openeel.credentials.passkey.CreatePasskeyUseCase
import org.openeel.credentials.passkey.OpenEelPasskeyCredential
import org.openeel.credentials.passkey.OpenEelPasswordCredential
import org.openeel.credentials.passkey.OpenEelQRBadgeCredential
import org.openeel.credentials.passkey.OpenEelUserHandle
import org.openeel.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.adapters.toEntity
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.school.adapters.toPersonPasskey
import org.openeel.datalayer.school.ext.accepterEnrollmentRole
import org.openeel.datalayer.school.ext.accepterPersonRole
import org.openeel.datalayer.school.ext.copyWithInviteInfo
import org.openeel.datalayer.school.ext.isApprovalRequiredNow
import org.openeel.datalayer.school.model.AuthToken
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.NewUserInvite
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.ClassInviteModeEnum
import org.openeel.datalayer.school.model.Enrollment
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.datalayer.school.model.StatusEnum
import org.openeel.libutil.ext.randomString
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.AuthResponse
import org.openeel.shared.domain.account.authwithpassword.GetTokenAndUserProfileWithCredentialDbImpl
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import org.openeel.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import org.openeel.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase
import org.openeel.shared.domain.school.SchoolPrimaryKeyGenerator
import org.openeel.shared.util.di.SchoolDataSourceLocalProvider
import org.openeel.shared.util.toPerson
import java.lang.IllegalArgumentException
import kotlin.time.Clock

/**
 * Server-side use case that handles redeeming an invite: that is when a (new) user client signing up
 * provies both a) invite info and code and b) information about the account they want to create (
 * name, gender, username, password/passkey, etc).
 */
class RedeemInviteUseCaseDb(
    private val schoolDb: SchoolDatabase,
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
        redeemRequest: RedeemInviteRequest
    ): AuthResponse {
        val inviteFromDb = schoolDb.getInviteEntityDao().getInviteByInviteCode(
            redeemRequest.code
        )?.toModel()
            ?: throw IllegalArgumentException("invite not found for code: ${redeemRequest.code}")
                .withHttpStatus(404)

        val accountGuid = redeemRequest.account.guid

        val approvalRequired = inviteFromDb.isApprovalRequiredNow()

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
                it.copyWithInviteInfo(invite = redeemRequest.invite)
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
            is OpenEelPasswordCredential -> {
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

            is OpenEelPasskeyCredential -> {
                val passkeyCreatedResult = CreatePasskeyUseCase.PasskeyCreatedResult(
                    openEelUserHandle = OpenEelUserHandle(
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

            is OpenEelQRBadgeCredential -> {
                throw IllegalArgumentException("Using a QR code badge to redeem invite for new account not yet supported")
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