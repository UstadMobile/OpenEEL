//Transient properties are used as documented below, cannot be removed because they are needed for serialization
@file:Suppress("CanBeParameter")

package world.respect.shared.navigation

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.lib.xapi.extensions.reportoptions.ReportFilter
import world.respect.lib.xapi.model.XapiActor
import world.respect.shared.ext.NextAfterScan
import world.respect.shared.viewmodel.catalog.PublicationsSelection
import world.respect.shared.viewmodel.manageuser.signup.SignupScreenModeEnum
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryMode
import world.respect.lib.opds.model.LangMap
import world.respect.shared.viewmodel.catalog.OpdsPickType
import kotlin.uuid.Uuid

@Serializable
sealed interface RespectAppRoute

@Serializable
data class Acknowledgement(
    val schoolUrlStr: String? = null,
    val inviteCode: String? = null
) : RespectAppRoute {

    @Transient
    val schoolUrl = schoolUrlStr?.let { Url(it) }

    companion object {
        fun create(schoolUrl: Url? = null, inviteCode: String? = null) =
            Acknowledgement(schoolUrl.toString(), inviteCode)
    }
}

@Serializable
data class EnterInviteCode(
    val schoolUrlStr: String
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(schoolUrl: Url) = EnterInviteCode(schoolUrl.toString())
    }
}

@Serializable
object Onboarding : RespectAppRoute

@Serializable
data class SchoolDirectoryList(
    val modeStr: String = SchoolDirectoryMode.MANAGE.value
) : RespectAppRoute {

    @Transient
    val mode: SchoolDirectoryMode = SchoolDirectoryMode.fromValue(modeStr)

    companion object {
        fun create(
            mode: SchoolDirectoryMode = SchoolDirectoryMode.MANAGE
        ) = SchoolDirectoryList(mode.value)
    }
}

@Serializable
object SchoolDirectoryEdit : RespectAppRoute

@Serializable
data class LoginScreen(
    val schoolUrlStr: String,
    val isSharedDevice: Boolean? = null,
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(schoolUrl: Url, isSharedDevice: Boolean? = null) =
            LoginScreen(schoolUrl.toString(), isSharedDevice)
    }
}

@Serializable
object Home : RespectAppRoute

@Serializable
data class RespectAppLauncher(
    val resultDestStr: String? = null,
    private val opdsPickTypeStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val opdsPickType: OpdsPickType? = opdsPickTypeStr?.let {
        Json.decodeFromString(OpdsPickType.serializer(), it)
    }

    companion object {
        fun create(
            resultDest: ResultDest? = null,
            opdsPickType: OpdsPickType? = null,
        ) = RespectAppLauncher(
            resultDestStr = resultDest.encodeToJsonStringOrNull(),
            opdsPickTypeStr = opdsPickType?.let {
                Json.encodeToString(OpdsPickType.serializer(), it)
            }
        )
    }
}

@Serializable
object AssignmentList : RespectAppRoute

@Serializable
data class AssignmentDetail(
    val assignmentActivityId: String,
) : RespectAppRoute

@Serializable
data class AssignmentEdit(
    val assignmentActivityId: String?,
    private val learningUnitStr: String? = null,
) : RespectAppRoute {

    @Transient
    val learningUnitSelected: PublicationsSelection? = learningUnitStr?.let {
        Json.decodeFromString(PublicationsSelection.serializer(), it)
    }

    companion object {
        fun create(
            assignmentActivityId: String?,
            learningUnitSelected: PublicationsSelection? = null,
        ) = AssignmentEdit(
            assignmentActivityId = assignmentActivityId,
            learningUnitStr = learningUnitSelected?.let {
                Json.encodeToString(PublicationsSelection.serializer(), it)
            },
        )
    }
}

@Serializable
data class BookmarkList(
    val resultDestStr: String? = null,
    val opdsPickTypeStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val opdsPickType: OpdsPickType? = opdsPickTypeStr?.let {
        Json.decodeFromString(OpdsPickType.serializer(), it)
    }


}

@Serializable
data class StatementList(
    val activityId: String,
    private val xapiActorStr: String,
) : RespectAppRoute {

    @Transient
    val xapiActor: XapiActor = Json.decodeFromString(XapiActor.serializer(), xapiActorStr)

    companion object {
        fun create(activityId: String, xapiActor: XapiActor) = StatementList(
            activityId = activityId,
            xapiActorStr = Json.encodeToString(XapiActor.serializer(), xapiActor)
        )
    }
}

@Serializable
data class StatementDetail(
    val statementId: String,
) : RespectAppRoute

@Serializable
data class RawStatement(
    val statementIdStr: String,
): RespectAppRoute {

    @Transient
    val statementId = Uuid.parse(statementIdStr)

    companion object {
        fun create(statementId: Uuid) = RawStatement(statementId.toString())
    }
}

@Serializable
object ClazzList : RespectAppRoute

@Serializable
class ClazzDetail(
    val guid: String,
) : RespectAppRoute

@Serializable
data class EnrollmentList(
    val filterByPersonUid: String,
    val roleStr: String,
    val filterByClassUid: String
) : RespectAppRoute {

    @Transient
    val role = EnrollmentRoleEnum.fromValue(roleStr)

    companion object {
        fun create(
            filterByPersonUid: String,
            role: EnrollmentRoleEnum,
            filterByClassUid: String
        ): EnrollmentList {
            return EnrollmentList(
                filterByPersonUid = filterByPersonUid,
                roleStr = role.value,
                filterByClassUid = filterByClassUid
            )
        }
    }
}

@Serializable
data class EnrollmentEdit(
    val uid: String?,
    val role: String,
    val personGuid: String,
    val clazzGuid: String,
) : RespectAppRoute

@Serializable
class AddPersonToClazz(
    val roleTypeStr: String,
    val inviteCode: String? = null,
) : RespectAppRoute {

    @Transient
    val roleType = EnrollmentRoleEnum.fromValue(roleTypeStr)

    companion object {
        fun create(
            roleType: EnrollmentRoleEnum,
            inviteCode: String?,
        ) = AddPersonToClazz(
            roleTypeStr = roleType.value,
            inviteCode = inviteCode,
        )
    }
}

@Serializable
data class ClazzEdit(
    val guid: String?
) : RespectAppRoute

@Serializable
object Report : RespectAppRoute

@Serializable
class ReportEdit(val reportUid: String?) : RespectAppRoute

@Serializable
class ReportDetail(val reportUid: String) : RespectAppRoute

@Serializable
class ReportEditFilter(
    private val reportFilterJson: String
) : RespectAppRoute {

    @Transient
    val reportFilter: ReportFilter =
        Json.decodeFromString(ReportFilter.serializer(), reportFilterJson)

    companion object {
        fun create(reportFilter: ReportFilter): ReportEditFilter {
            val jsonStr = Json.encodeToString(ReportFilter.serializer(), reportFilter)
            return ReportEditFilter(jsonStr)
        }
    }
}

@Serializable
object ReportTemplateList : RespectAppRoute

@Serializable
object IndicatorList : RespectAppRoute

@Serializable
class IndicatorDetail(val indicatorUid: String) : RespectAppRoute

@Serializable
class IndictorEdit(val indicatorId: String?) : RespectAppRoute

@Serializable
object RespectAppList : RespectAppRoute

@Serializable
object EnterLink : RespectAppRoute

@Serializable
data class GetStartedScreen(
    val canGoBack: Boolean = false,
) : RespectAppRoute

@Serializable
object OtherOption : RespectAppRoute

@Serializable
object HowPasskeyWorks : RespectAppRoute

@Serializable
class AppsDetail private constructor(
    private val manifestUrlStr: String,
    private val resultDestStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    val manifestUrl = Url(manifestUrlStr)

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    companion object {
        fun create(
            manifestUrl: Url,
            resultDest: ResultDest? = null,
        ): AppsDetail {
            return AppsDetail(
                manifestUrlStr = manifestUrl.toString(),
                resultDestStr = resultDest?.encodeToJsonStringOrNull()
            )
        }
    }
}

@Serializable
class OpdsFeedDetail(
    private val opdsFeedUrlStr: String,
    private val resultDestStr: String? = null,
    private val opdsPickTypeStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    val opdsFeedUrl = Url(opdsFeedUrlStr)

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val opdsPickType = opdsPickTypeStr?.let { jsonStr ->
        Json.decodeFromString(
            OpdsPickType.serializer(),
            jsonStr,
        )
    }

    companion object {
        fun create(
            opdsFeedUrl: Url,
            resultDest: ResultDest? = null,
            opdsPickType: OpdsPickType? = null,
        ) = OpdsFeedDetail(
            opdsFeedUrlStr = opdsFeedUrl.toString(),
            resultDestStr = resultDest.encodeToJsonStringOrNull(),
            opdsPickTypeStr = opdsPickType?.let {
                Json.encodeToString(OpdsPickType.serializer(), it)
            },
        )
    }
}
@Serializable
class EnterPasswordSignup private constructor(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest: RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest,
        ): EnterPasswordSignup {
            return EnterPasswordSignup(
                schoolUrl.toString(),
                Json.encodeToString(inviteRequest)
            )
        }
    }
}

@Serializable
class OtherOptionsSignup private constructor(
    private val inviteRedeemRequestStr: String,
    private val schoolUrlStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest: RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest,
        ): OtherOptionsSignup {
            val respectRedeemInviteRequest = Json.encodeToString(inviteRequest)
            return OtherOptionsSignup(
                respectRedeemInviteRequest, schoolUrl.toString()
            )
        }
    }
}

@Serializable
class AcceptInvite(
    val schoolUrlStr: String,
    val code: String,
    val canGoBack: Boolean = true,
    val useActiveUserAuth: Boolean? = null,
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            code: String,
            canGoBack: Boolean = true,
            useActiveUserAuth: Boolean? = null,
        ) = AcceptInvite(
            schoolUrlStr = schoolUrl.toString(),
            code = code,
            canGoBack = canGoBack,
            useActiveUserAuth = useActiveUserAuth,
        )
    }
}

@Serializable
class WaitingForApproval : RespectAppRoute

@Serializable
class SignupScreen(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
    private val signupModeStr: String,
    private val parentPersonStr: String?,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest: RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val signupMode: SignupScreenModeEnum = SignupScreenModeEnum.fromValue(signupModeStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    @Transient
    val parentPerson: Person? = parentPersonStr?.let { Json.decodeFromString(it) }

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest,
            signupMode: SignupScreenModeEnum = SignupScreenModeEnum.STANDARD,
            parentPerson: Person? = null,
        ): SignupScreen {
            return SignupScreen(
                schoolUrlStr = schoolUrl.toString(),
                inviteRedeemRequestStr = Json.encodeToString(inviteRequest),
                signupModeStr = signupMode.value,
                parentPersonStr = parentPerson?.let { Json.encodeToString(it) }
            )
        }
    }
}

@Serializable
class TermsAndCondition(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest: RespectRedeemInviteRequest =
        Json.decodeFromString(inviteRedeemRequestStr)

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest
        ): TermsAndCondition {
            return TermsAndCondition(
                schoolUrlStr = schoolUrl.toString(),
                inviteRedeemRequestStr = Json.encodeToString(inviteRequest)
            )
        }
    }
}

@Serializable
data class SchoolRegistrationComplete(
    val schoolUrl: String = "",
    val authToken: String? = null
) : RespectAppRoute

@Serializable
class CreateAccount(
    private val schoolUrlStr: String,
    private val inviteRedeemRequestStr: String,
) : RespectAppRoute {

    @Transient
    val respectRedeemInviteRequest: RespectRedeemInviteRequest = Json.decodeFromString(
        inviteRedeemRequestStr
    )

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        fun create(
            schoolUrl: Url,
            inviteRequest: RespectRedeemInviteRequest
        ): CreateAccount {
            return CreateAccount(
                schoolUrlStr = schoolUrl.toString(),
                inviteRedeemRequestStr = Json.encodeToString(inviteRequest)
            )
        }
    }
}

@Serializable
class PublicationDetail(
    private val learningUnitManifestUrlStr: String,
    private val refererUrlStr: String? = null,
    val expectedIdentifier: String? = null,
    val assignmentActivityId: String? = null,
    private val titleStr: String? = null,
) : RespectAppRoute {

    @Transient
    val learningUnitManifestUrl = Url(learningUnitManifestUrlStr)

    @Transient
    val refererUrl = refererUrlStr?.let { Url(it) }

    @Transient
    val title: LangMap? = titleStr?.let { Json.decodeFromString(LangMap.serializer(), it) }

    companion object {
        fun create(
            learningUnitManifestUrl: Url,
            refererUrl: Url? = null,
            expectedIdentifier: String? = null,
            assignmentActivityId: String? = null,
            title: LangMap? = null,
        ) = PublicationDetail(
            learningUnitManifestUrlStr = learningUnitManifestUrl.toString(),
            refererUrlStr = refererUrl?.toString(),
            expectedIdentifier = expectedIdentifier,
            assignmentActivityId = assignmentActivityId,
            titleStr = title?.let { Json.encodeToString(LangMap.serializer(), it) },
        )
    }
}

@Serializable
class LearningUnitViewer(
    private val learningUnitIdStr: String,
) : RespectAppRoute {

    @Transient
    val learningUnitId = Url(learningUnitIdStr)

    companion object {
        fun create(learningUnitId: Url): LearningUnitViewer {
            return LearningUnitViewer(
                learningUnitIdStr = learningUnitId.toString()
            )
        }
    }
}

@Serializable
object AccountList : RespectAppRoute

@Serializable
object ShareFeedback : RespectAppRoute

/**
 * @property addToClassUid if the PersonList screen has been navigated when the user clicks
 *           add student or add teacher on the ClassDetail screen, then the classUid.
 * @property addToClassRoleStr if the PersonList screen has been navigated when the user clicks
 *  *           add student or add teacher on the ClassDetail screen, then the role
 */
@Serializable
data class PersonList(
    private val filterByRoleStr: String? = null,
    val isTopLevel: Boolean = false,
    private val resultDestStr: String? = null,
    val inviteUid: String? = null,
    val classNameStr: String? = null,
    val addToClassUid: String? = null,
    val addToClassRoleStr: String? = null,
    val personGuidStr: String? = null,
    val hideInvite: Boolean = false,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    val filterByRole: PersonRoleEnum? = filterByRoleStr?.let {
        PersonRoleEnum.fromValue(it)
    }

    @Transient
    val role: EnrollmentRoleEnum? = addToClassRoleStr?.let {
        EnrollmentRoleEnum.fromValue(it)
    }

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    companion object {
        fun create(
            filterByRole: PersonRoleEnum? = null,
            isTopLevel: Boolean = false,
            resultDest: ResultDest? = null,
            inviteUid: String? = null,
            className: String? = null,
            classUid: String? = null,
            personGuid: String? = null,
            addToClassRole: EnrollmentRoleEnum? = null,
            hideInvite: Boolean = false,
        ) = PersonList(
            filterByRoleStr = filterByRole?.value,
            isTopLevel = isTopLevel,
            resultDestStr = resultDest.encodeToJsonStringOrNull(),
            inviteUid = inviteUid,
            addToClassUid = classUid,
            classNameStr = className,
            addToClassRoleStr = addToClassRole?.value,
            personGuidStr = personGuid,
            hideInvite = hideInvite,
        )
    }
}

@Serializable
data class PersonDetail(
    val guid: String,
) : RespectAppRoute

@Serializable
data class PasskeyList(
    val guid: String,
) : RespectAppRoute

@Serializable
data class ManageAccount(
    val guid: String,
    val setPersonQrBadgeUsername: String? = null,
    private val setPersonQrBadgeUrlStr: String? = null,
) : RespectAppRoute {

    @Transient
    val setPersonQrBadgeUrl: Url? = setPersonQrBadgeUrlStr?.let { Url(it) }

    companion object {
        fun create(
            guid: String,
            qrUrl: Url? = null,
            username: String? = null,
        ) = ManageAccount(
            guid = guid,
            setPersonQrBadgeUrlStr = qrUrl?.toString(),
            setPersonQrBadgeUsername = username,
        )
    }
}

@Serializable
data class PersonEdit(
    val guid: String?,
    private val resultDestStr: String? = null,
    private val presetRoleStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val presetRole: PersonRoleEnum? = presetRoleStr?.let {
        PersonRoleEnum.fromValue(it)
    }

    companion object {
        fun create(
            guid: String?,
            resultDest: ResultDest? = null,
            presetRole: PersonRoleEnum? = null,
        ) = PersonEdit(
            guid = guid,
            resultDestStr = resultDest.encodeToJsonStringOrNull(),
            presetRoleStr = presetRole?.value,
        )
    }
}

@Serializable
data object Settings : RespectAppRoute

@Serializable
data class ScanQRCode(
    val guid: String? = null,
    val resultDestStr: String? = null,
    private val schoolUrlStr: String? = null,
    val username: String? = null,
    private val nextAfterScanStr: String? = null,
    val isSharedDevice: Boolean = false
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val schoolUrl: Url? = schoolUrlStr?.let { Url(it) }

    @Transient
    val nextAfterScan: NextAfterScan? = nextAfterScanStr?.let {
        NextAfterScan.valueOf(it)
    }

    companion object {
        fun create(
            guid: String? = null,
            resultDest: ResultDest? = null,
            schoolUrl: Url? = null,
            username: String? = null,
            nextAfterScan: NextAfterScan? = null,
            isSharedDevice: Boolean = false
        ) = ScanQRCode(
            guid = guid,
            resultDestStr = resultDest?.encodeToJsonStringOrNull(),
            username = username,
            schoolUrlStr = schoolUrl?.toString(),
            nextAfterScanStr = nextAfterScan?.name,
            isSharedDevice = isSharedDevice
        )
    }
}
@Serializable
class PlaylistList private constructor(
    private val resultDestStr: String? = null,
    val opdsPickTypeStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    @Transient
    val opdsPickType: OpdsPickType? = opdsPickTypeStr?.let {
        Json.decodeFromString(OpdsPickType.serializer(), it)
    }

    companion object {
        fun create(
            resultDest: ResultDest? = null,
            opdsPickType: OpdsPickType? = null,
        ) = PlaylistList(
            resultDestStr = resultDest.encodeToJsonStringOrNull(),
            opdsPickTypeStr = opdsPickType?.let {
                Json.encodeToString(OpdsPickType.serializer(), it)
            }
        )
    }
}
@Serializable
data object SchoolSettings : RespectAppRoute

@Serializable
data object SharedDevicesSettings : RespectAppRoute

@Serializable
data class SelectClass(
    val deviceGuid: String
) : RespectAppRoute {

    companion object {
        fun create(
            deviceGuid: String
        ) = SelectClass(
            deviceGuid = deviceGuid
        )
    }
}

@Serializable
data object TeacherPinConfirmation : RespectAppRoute

@Serializable
data class StudentList(
    val className: String,
    val guid: String,
) : RespectAppRoute {

    companion object {
        fun create(
            className: String,
            guid: String,
        ) = StudentList(
            className = className,
            guid = guid,
        )
    }
}

@Serializable
class OpdsFeedEdit private constructor(
    private val urlStr: String? = null,
    val isCopy: Boolean = false,
) : RespectAppRoute {

    @Transient
    val url: Url? = urlStr?.let { Url(it) }

    companion object {
        fun create(
            playlistUrl: Url? = null,
            isCopy: Boolean = false,
        ) = OpdsFeedEdit(
            urlStr = playlistUrl?.toString(),
            isCopy = isCopy,
        )
    }
}
@Serializable
class PlaylistShare private constructor(
    private val playlistUrlStr: String,
) : RespectAppRoute {

    @Transient
    val playlistUrl = Url(playlistUrlStr)

    companion object {
        fun create(playlistUrl: Url) = PlaylistShare(
            playlistUrlStr = playlistUrl.toString()
        )
    }
}

@Serializable
class ExternalLinkEdit private constructor(
    private val resultDestStr: String? = null,
) : RespectAppRoute, RouteWithResultDest {

    @Transient
    override val resultDest: ResultDest? = ResultDest.fromStringOrNull(resultDestStr)

    companion object {
        fun create(
            resultDest: ResultDest? = null,
        ) = ExternalLinkEdit(
            resultDestStr = resultDest.encodeToJsonStringOrNull()
        )
    }
}

@Serializable
data class CreateAccountSetUsername(
    val guid: String
): RespectAppRoute

@Serializable
data class CreateAccountSetPassword(
    val guid: String,
    val username: String? = null,
) : RespectAppRoute

@Serializable
data class ChangePassword(
    val guid: String,
) : RespectAppRoute

@Serializable
data class InvitePerson(
    val invitePersonOptionsStr: String,
) : RespectAppRoute {

    @Serializable
    sealed interface InvitePersonOptions

    @Serializable
    @SerialName("newuser")
    data class NewUserInviteOptions(
        val presetRole: PersonRoleEnum?
    ) : InvitePersonOptions

    @Serializable
    @SerialName("class")
    data class ClassInviteOptions(
        val inviteUid: String,
    ) : InvitePersonOptions

    @Transient
    val invitePersonOptions: InvitePersonOptions = Json.decodeFromString(
        invitePersonOptionsStr
    )

    companion object {
        fun create(
            invitePersonOptions: InvitePersonOptions
        ) = InvitePerson(
            invitePersonOptionsStr = Json.encodeToString(invitePersonOptions)
        )
    }
}

@Serializable
data class QrCode(
    val inviteLink: String? = null,
    val schoolOrClass: String? = null
) : RespectAppRoute

@Serializable
data class CopyCode(
    val inviteCode:String?=null
): RespectAppRoute

@Serializable
data class SendDbToServer(
    val schoolUrlStr: String,
    val name: String,
) : RespectAppRoute {

    @Transient
    val schoolUrl = Url(schoolUrlStr)

    companion object {
        const val DEEP_LINK_PATH = "senddbtoserver"
        const val QUERY_PARAM_NAME = "name"

        fun create(schoolUrl: Url, name: String) = SendDbToServer(
            schoolUrlStr = schoolUrl.toString(),
            name = name,
        )
    }
}
