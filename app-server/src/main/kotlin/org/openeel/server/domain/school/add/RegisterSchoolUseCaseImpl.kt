package org.openeel.server.domain.school.add

import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.school.ext.newUserInviteUid
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.NewUserInvite
import org.openeel.app.userdirectory.model.PersonRoleEnum
import org.openeel.datalayer.school.model.StatusEnum
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.libutil.ext.normalizeForEndpoint
import org.openeel.libutil.ext.sanitizedForFilename
import org.openeel.server.SchoolConfig
import org.openeel.server.domain.school.verify.VerifySchoolUrlPointsToThisServerUseCase
import org.openeel.server.util.ext.HttpStatusException
import org.openeel.shared.domain.account.invite.CreateInviteUseCase
import org.openeel.shared.domain.createlink.CreateInviteLinkUseCase
import org.openeel.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase
import org.openeel.shared.domain.school.add.RegisterSchoolUseCase
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class RegisterSchoolUseCaseImpl(
    private val registerSchoolPin: String?,
) : RegisterSchoolUseCase, KoinComponent {

    private val addSchoolUseCase: AddSchoolUseCase by inject()
    private val schoolConfig: SchoolConfig by inject()
    private val urlToCustomDeepLinkUseCase: UrlToCustomDeepLinkUseCase  by inject()
    private val verifySchoolUrlUseCase: VerifySchoolUrlPointsToThisServerUseCase by inject()

    override suspend operator fun invoke(
        request: RegisterSchoolUseCase.RegisterSchoolRequest
    ): RegisterSchoolUseCase.RegisterSchoolResponse {
        if (!schoolConfig.registration.enabled) {
            throw HttpStatusException("School registration is disabled", HttpStatusCode.Forbidden)
        }

        // Validate inputs
        if (request.schoolName.isBlank() || request.schoolUrl.isBlank()) {
            throw HttpStatusException("School name and URL are required", HttpStatusCode.BadRequest)
        }

        // Parse and validate URL
        val parsedUrl = try {
            Url(request.schoolUrl).normalizeForEndpoint()
        } catch (e: Exception) {
            throw HttpStatusException(
                "Invalid URL format: ${request.schoolUrl}",
                HttpStatusCode.BadRequest
            )
        }

        // Verify school URL - will throw SchoolUrlVerificationException if verification fails
        verifySchoolUrlUseCase(parsedUrl)

        val adminUsername = request.adminUsername
        val adminPassword = request.adminPassword

        if(adminUsername != null && adminPassword != null &&
            (request.schoolCreationPin == null || request.schoolCreationPin != registerSchoolPin)
        ) {
            throw HttpStatusException("Invalid school creation PIN", HttpStatusCode.Forbidden)
        }

        // Create school using AddSchoolUseCase
        addSchoolUseCase(
            listOf(
                AddSchoolUseCase.AddSchoolRequest(
                    school = SchoolDirectoryEntry(
                        name = LangMapStringValue(request.schoolName),
                        self = parsedUrl,
                        xapi = Url("${request.schoolUrl}/api/school/xapi"),
                        respectExt = Url("${request.schoolUrl}/api/school/respect"),
                        rpId = parsedUrl.host,
                        lastModified = Clock.System.now(),
                        stored = Clock.System.now(),
                        inDirectoryUrl = request.inDirectoryUrl,
                    ),
                    dbUrl = parsedUrl.sanitizedForFilename(),
                    adminUsername = adminUsername,
                    adminPassword = adminPassword,
                )
            )
        )

        val schoolScopeId = SchoolDirectoryEntryScopeId(parsedUrl, null)
        val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(schoolScopeId.scopeId)

        return if(adminUsername != null && adminPassword != null) {
            RegisterSchoolUseCase.RegisterSchoolResponse(
                schoolName = request.schoolName,
                schoolUrl = Url(request.schoolUrl),
                adminUsername = adminUsername,
                inDirectoryUrl = request.inDirectoryUrl,
            )
        }else {
            val createInviteUseCase: CreateInviteUseCase = schoolScope.get()
            val createInviteLinkUseCase: CreateInviteLinkUseCase = schoolScope.get()


            val inviteCode = Invite2.newRandomCode()

            val tenYearsFromNow = Clock.System.now() + (10 * 365).days
            val firstUserInviteUid = "${PersonRoleEnum.SYSTEM_ADMINISTRATOR.newUserInviteUid}:first"

            val invite = NewUserInvite(
                uid = firstUserInviteUid,
                code = inviteCode,
                role = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                firstUser = true,
                status = StatusEnum.ACTIVE,
                lastModified = Clock.System.now(),
                stored = Clock.System.now(),
                approvalRequiredAfter = tenYearsFromNow
            )

            createInviteUseCase(invite)

            // Create the regular invite URL first
            val regularInviteUrl = createInviteLinkUseCase(inviteCode)

            // Convert to custom deep link so it opens directly in the app
            val customDeepLinkUrl = urlToCustomDeepLinkUseCase(regularInviteUrl)

            RegisterSchoolUseCase.RegisterSchoolResponse(
                schoolName = request.schoolName,
                schoolUrl = Url(request.schoolUrl),
                redirectUrl = customDeepLinkUrl,
                inDirectoryUrl = request.inDirectoryUrl,
            )
        }
    }

}

class SchoolRegistrationDisabledException(message: String) :
    HttpStatusException(message, HttpStatusCode.Forbidden)

class InvalidSchoolRegistrationRequestException(message: String) :
    HttpStatusException(message, HttpStatusCode.BadRequest)