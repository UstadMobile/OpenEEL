
package world.respect

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import world.respect.shared.domain.phonenumber.IPhoneNumberUtil
import world.respect.shared.domain.phonenumber.IPhoneNumberUtilAndroid
import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase
import com.ustadmobile.libcache.CachePathsProvider
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.connectivitymonitor.ConnectivityMonitor
import com.ustadmobile.libcache.connectivitymonitor.ConnectivityMonitorAndroid
import com.ustadmobile.libcache.db.ClearNeighborsCallback
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.migrations.addCacheDbMigrations
import com.ustadmobile.libcache.downloader.EnqueueRunDownloadJobUseCase
import com.ustadmobile.libcache.downloader.EnqueueRunDownloadJobUseCaseAndroid
import com.ustadmobile.libcache.downloader.PinPublicationPrepareUseCase
import com.ustadmobile.libcache.downloader.RunDownloadJobUseCase
import com.ustadmobile.libcache.downloader.RunDownloadJobUseCaseImpl
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import com.ustadmobile.libcache.webview.OkHttpWebViewClient
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import world.respect.app.BuildConfig
import world.respect.callback.AddDirectoriesFromPropertiesUseCase
import world.respect.callback.AddSchoolDirectoryCallback
import world.respect.callback.migrate6to8AddDirectories
import world.respect.credentials.passkey.CheckPasskeySupportUseCase
import world.respect.credentials.passkey.CheckPasskeySupportUseCaseAndroidImpl
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.CreatePasskeyUseCaseAndroidChannelHost
import world.respect.credentials.passkey.CreatePasskeyUseCaseAndroidImpl
import world.respect.credentials.passkey.GetCredentialUseCase
import world.respect.credentials.passkey.GetCredentialUseCaseAndroidImpl
import world.respect.credentials.passkey.VerifyDomainUseCase
import world.respect.credentials.passkey.VerifyDomainUseCaseImpl
import world.respect.credentials.passkey.password.SavePasswordUseCase
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialCreationOptionsJsonUseCase
import world.respect.credentials.passkey.request.CreatePublicKeyCredentialRequestOptionsJsonUseCase
import world.respect.credentials.passkey.request.EncodeUserHandleUseCase
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import world.respect.credentials.password.SavePasswordUseCaseAndroidImpl
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectAppDataSourceDb
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.addCommonMigrations
import world.respect.datalayer.db.networkvalidation.ExtendedDataSourceValidationHelperImpl
import world.respect.datalayer.db.school.GetAuthenticatedPersonUseCase
import world.respect.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import world.respect.datalayer.db.school.writequeue.RemoteWriteQueueDbImpl
import world.respect.datalayer.db.school.xapi.writequeue.XapiRemoteWriteQueueDbImpl
import world.respect.datalayer.db.schooldirectory.SchoolDirectoryDataSourceDb
import world.respect.datalayer.db.shared.PullSyncTrackerDbImpl
import world.respect.datalayer.http.RespectAppDataSourceHttp
import world.respect.datalayer.http.SchoolDataSourceHttpClient
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.RespectAppDataSourceRepository
import world.respect.datalayer.repository.SchoolDataSourceRepository
import world.respect.datalayer.repository.school.pullsync.EnqueueRunPullSyncUseCaseAndroidImpl
import world.respect.datalayer.repository.school.pullsync.RunPullSyncUseCase
import world.respect.datalayer.repository.school.writequeue.DrainRemoteWriteQueueUseCase
import world.respect.datalayer.repository.school.writequeue.EnqueueDrainRemoteWriteQueueUseCaseAndroidImpl
import world.respect.datalayer.repository.school.writequeue.EnqueueDrainXapiRemoteWriteQueueUseCaseAndroidImpl
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.domain.GetWritableRolesListUseCase
import world.respect.datalayer.school.domain.GetWritableRolesListUseCaseImpl
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase
import world.respect.datalayer.school.writequeue.EnqueueRunPullSyncUseCase
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.lib.xapi.remotewritequeue.DrainXapiRemoteWriteQueueUseCase
import world.respect.lib.xapi.remotewritequeue.EnqueueDrainXapiRemoteWriteQueueUseCase
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.shared.pullsync.PullSyncTracker
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.libxxhash.XXHasher64Factory
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXHasher64FactoryCommonJvm
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectAccountSchoolScopeLink
import world.respect.shared.domain.account.RespectTokenManager
import world.respect.shared.domain.account.child.AddChildAccountUseCase
import world.respect.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import world.respect.shared.domain.account.child.AddChildAccountUseCaseClient
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCaseClient
import world.respect.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.account.invite.GetInviteInfoUseCaseClient
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.account.invite.RedeemInviteUseCaseClient
import world.respect.shared.domain.navigation.onaccountcreated.NavigateOnAccountCreatedUseCase
import world.respect.shared.domain.account.passkey.EncodeUserHandleUseCaseImpl
import world.respect.shared.domain.account.passkey.GetPasskeyProviderInfoUseCaseImpl
import world.respect.shared.domain.account.passkey.GetActivePersonPasskeysClient
import world.respect.shared.domain.account.passkey.GetActivePersonPasskeysUseCase
import world.respect.shared.domain.account.passkey.LoadAaguidJsonUseCase
import world.respect.shared.domain.account.passkey.LoadAaguidJsonUseCaseAndroid
import world.respect.shared.domain.account.passkey.RevokePasskeyUseCase
import world.respect.shared.domain.account.passkey.RevokePasskeyUseCaseClient
import world.respect.shared.domain.account.passkey.VerifyPasskeyUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import world.respect.shared.domain.account.setpassword.EncryptPersonPasswordUseCaseImpl
import world.respect.shared.domain.account.username.UsernameSuggestionUseCase
import world.respect.shared.domain.account.username.UsernameSuggestionUseCaseClient
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCaseClient
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.domain.account.validatepassword.ValidatePasswordUseCase
import world.respect.shared.domain.account.validateqrbadge.ValidateQrCodeUseCase
import world.respect.shared.domain.appversioninfo.GetAppVersionInfoUseCase
import world.respect.shared.domain.appversioninfo.GetAppVersionInfoUseCaseAndroid
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.domain.clipboard.SetClipboardStringUseCaseAndroid
import world.respect.shared.domain.geolookup.GetCountryForUrlUseCase
import world.respect.shared.domain.geolookup.GetCountryForUrlUseCaseImpl
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.domain.devmode.GetDevModeEnabledUseCase
import world.respect.shared.domain.devmode.SetDevModeEnabledUseCase
import world.respect.shared.domain.school.LaunchCustomTabUseCaseAndroid
import world.respect.app.domain.e2eartifactupload.GetDbFilesForE2EArtifactUploadUseCaseAndroid
import world.respect.datalayer.db.APP_MIGRATION_8_9_CLIENT
import world.respect.shared.domain.account.invite.EnableSharedDeviceModeUseCase
import world.respect.shared.domain.account.sharedschooldevice.GetSharedDeviceSelfSelectUseCase
import world.respect.shared.domain.account.sharedschooldevice.SetSharedDeviceSelfSelectUseCase
import world.respect.shared.domain.account.sharedschooldevice.setpin.GetSharedDevicePINUseCase
import world.respect.shared.domain.account.sharedschooldevice.setpin.GetSharedDevicePINUseCaseImpl
import world.respect.shared.domain.account.sharedschooldevice.setpin.SetSharedDevicePINUseCase
import world.respect.shared.domain.account.sharedschooldevice.setpin.SetSharedDevicePINUseCaseImpl
import world.respect.shared.domain.activitycontextjobprocessor.EnqueueActivityContextJobUseCase
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCaseAndroid
import world.respect.shared.domain.e2eartifactupload.GetDbFilesForE2EArtifactUploadUseCase
import world.respect.shared.domain.e2eartifactupload.E2EArtifactUploadUseCase
import world.respect.shared.domain.e2eartifactupload.E2EArtifactUploadUseCaseClient
import world.respect.shared.domain.getwarnings.GetWarningsUseCase
import world.respect.shared.domain.getwarnings.GetWarningsUseCaseAndroid
import world.respect.shared.domain.license.GetLicenseLabelUseCaseAndroid
import world.respect.shared.domain.bookmark.AddBookmarkUseCase
import world.respect.shared.domain.bookmark.RemoveBookmarkUseCase
import world.respect.shared.domain.launchapp.LaunchAppUseCase
import world.respect.shared.domain.launchapp.LaunchAppUseCaseAndroid
import world.respect.shared.domain.launchers.LaunchSendWhatsAppUseCase
import world.respect.shared.domain.launchers.LaunchSendWhatsAppUseCaseAndroid
import world.respect.shared.domain.openexternallink.OpenExternalLinkUseCase
import world.respect.shared.domain.launchers.OpenExternalLinkUseCaseAndroid
import world.respect.shared.domain.externallink.ExtractWebPageMetadataUseCase
import world.respect.shared.domain.externallink.ExtractWebPageMetadataUseCaseAndroid
import world.respect.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import world.respect.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.domain.permissions.CheckSchoolPermissionsUseCase
import world.respect.shared.domain.phonenumber.OnClickPhoneNumUseCase
import world.respect.shared.domain.phonenumber.OnClickPhoneNumberUseCaseAndroid
import world.respect.shared.domain.phonenumber.PhoneNumValidatorAndroid
import world.respect.shared.domain.phonenumber.PhoneNumValidatorUseCase
import world.respect.shared.domain.report.formatter.CreateGraphFormatterUseCase
import world.respect.shared.domain.report.query.MockRunReportUseCaseClientImpl
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.domain.school.LaunchCustomTabUseCase
import world.respect.shared.domain.school.RespectSchoolPath
import world.respect.shared.domain.school.SchoolDbPath
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.domain.storage.CachePathsProviderAndroid
import world.respect.shared.domain.storage.GetAndroidSdCardDirUseCase
import world.respect.shared.domain.storage.GetOfflineStorageOptionsUseCaseAndroid
import world.respect.shared.domain.storage.GetOfflineStorageSettingUseCase
import world.respect.shared.domain.usagereporting.GetUsageReportingEnabledUseCase
import world.respect.shared.domain.usagereporting.GetUsageReportingEnabledUseCaseAndroid
import world.respect.shared.domain.usagereporting.SetUsageReportingEnabledUseCase
import world.respect.shared.domain.usagereporting.SetUsageReportingEnabledUseCaseAndroid
import world.respect.shared.domain.validateemail.ValidateEmailUseCase
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.NavResultReturnerImpl
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.app.appstate.SnackBarFlowDispatcher
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailViewModel
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import world.respect.shared.viewmodel.assignment.list.AssignmentListViewModel
import world.respect.shared.viewmodel.enrollment.list.EnrollmentListViewModel
import world.respect.shared.viewmodel.enrollment.edit.EnrollmentEditViewModel
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel
import world.respect.shared.viewmodel.clazz.list.ClazzListViewModel
import world.respect.shared.viewmodel.catalog.publicationdetail.PublicationDetailViewModel
import world.respect.shared.viewmodel.catalog.opdsfeeddetail.OpdsFeedDetailViewModel
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListViewModel
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel
import world.respect.shared.viewmodel.manageuser.acceptinvite.AcceptInviteViewModel
import world.respect.shared.viewmodel.manageuser.enterpasswordsignup.EnterPasswordSignupViewModel
import world.respect.shared.viewmodel.manageuser.getstarted.GetStartedViewModel
import world.respect.shared.viewmodel.manageuser.howpasskeywork.HowPasskeyWorksViewModel
import world.respect.shared.viewmodel.manageuser.enterinvitecode.EnterInviteCodeViewModel
import world.respect.shared.viewmodel.manageuser.login.LoginViewModel
import world.respect.shared.viewmodel.manageuser.otheroption.OtherOptionsViewModel
import world.respect.shared.viewmodel.manageuser.otheroptionsignup.OtherOptionsSignupViewModel
import world.respect.shared.viewmodel.manageuser.profile.SignupViewModel
import world.respect.shared.viewmodel.manageuser.signup.CreateAccountViewModel
import world.respect.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionViewModel
import world.respect.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel
import world.respect.shared.viewmodel.onboarding.OnboardingViewModel
import world.respect.shared.viewmodel.person.changepassword.ChangePasswordViewModel
import world.respect.shared.viewmodel.person.copycode.CopyInviteCodeViewModel
import world.respect.shared.viewmodel.person.detail.PersonDetailViewModel
import world.respect.shared.domain.biometric.BiometricAuthUseCase
import world.respect.shared.domain.biometric.BiometricAuthUseCaseAndroidImpl
import world.respect.shared.domain.catalog.saveopdsfeed.SaveOpdsFeedUseCase
import world.respect.shared.domain.createclass.CreateClassUseCase
import world.respect.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase
import world.respect.shared.domain.geticonforxapiactivity.GetPublicationForXapiActivityUseCase
import world.respect.shared.domain.getlanguageendonym.GetLanguageEndonymUseCase
import world.respect.shared.domain.launchapp.GetAndroidPackageIdForLaunchableAppUseCase
import world.respect.shared.domain.launchapp.getlaunchoptionsforpublication.GetLaunchOptionsForPublicationUseCase
import world.respect.shared.domain.launchapp.getxapilaunchparams.GetXapiLaunchParamsUseCase
import world.respect.shared.domain.launchapp.getxapilaunchparams.GetXapiLaunchParamsUseCaseAndroid
import world.respect.shared.domain.launchapp.gotoappstore.GoToAppStoreUseCase
import world.respect.shared.domain.launchapp.gotoappstore.GoToAppStoreUseCaseAndroid
import world.respect.shared.domain.license.GetLicenseLabelUseCase
import world.respect.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCase
import world.respect.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCase
import world.respect.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCaseAndroid
import world.respect.shared.viewmodel.person.edit.PersonEditViewModel
import world.respect.shared.viewmodel.person.list.PersonListViewModel
import world.respect.shared.viewmodel.person.inviteperson.InvitePersonViewModel
import world.respect.shared.viewmodel.person.qrcode.InviteQrViewModel
import world.respect.shared.viewmodel.person.manageaccount.ManageAccountViewModel
import world.respect.shared.viewmodel.person.passkeylist.PasskeyListViewModel
import world.respect.shared.viewmodel.report.ReportViewModel
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel
import world.respect.shared.viewmodel.report.filteredit.ReportFilterEditViewModel
import world.respect.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel
import world.respect.shared.viewmodel.report.indictor.edit.IndicatorEditViewModel
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListViewModel
import world.respect.shared.viewmodel.report.list.ReportListViewModel
import world.respect.shared.viewmodel.report.list.ReportTemplateListViewModel
import world.respect.sharedse.domain.account.authenticatepassword.AuthenticatePasswordUseCaseDbImpl
import java.io.File
import world.respect.shared.viewmodel.settings.SettingsViewModel
import world.respect.shared.viewmodel.person.setusernameandpassword.CreateAccountSetPasswordViewModel
import world.respect.shared.viewmodel.person.setusernameandpassword.CreateAccountSetUserNameViewModel
import world.respect.shared.viewmodel.catalog.opdsfeededit.OpdsFeedEditViewModel
import world.respect.shared.viewmodel.catalog.opdsfeededitaddlink.OpdsFeedEditAddLinkViewModel
import world.respect.shared.viewmodel.catalog.opdsfeedlist.OpdsFeedListViewModel
import world.respect.shared.viewmodel.catalog.opdsfeedshare.OpdsFeedShareViewModel
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditViewModel
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListViewModel
import world.respect.shared.domain.sharelink.LaunchSendEmailUseCase
import world.respect.shared.domain.sharelink.LaunchShareLinkUseCase
import world.respect.shared.domain.sharelink.LaunchSendSmsUseCase
import world.respect.shared.domain.sendinvite.LaunchSendSmsAndroid
import world.respect.shared.domain.sendinvite.LaunchSendEmailAndroid
import world.respect.shared.domain.sendinvite.LaunchShareLinkAndroid
import world.respect.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import world.respect.shared.viewmodel.scanqrcode.ScanQRCodeViewModel
import world.respect.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCaseAndroid
import world.respect.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import world.respect.shared.domain.opds.getxapiactivityid.GetXapiActivityForPublicationUseCase
import world.respect.shared.viewmodel.statement.detail.RawStatementViewModel
import world.respect.shared.viewmodel.statement.detail.StatementDetailViewModel
import world.respect.shared.viewmodel.statement.list.StatementListViewModel
import world.respect.shared.domain.xapi.xapinanohttpd.XapiResourceProviderAndroid
import world.respect.shared.viewmodel.catalog.bookmark.BookmarkListViewModel
import world.respect.shared.viewmodel.sharedschooldevice.SchoolSettingsViewModel
import world.respect.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsViewmodel
import world.respect.shared.viewmodel.sharedschooldevice.TeacherPinConfirmationViewmodel
import world.respect.shared.viewmodel.sharedschooldevice.login.SelectClassViewModel
import world.respect.shared.viewmodel.sharedschooldevice.login.StudentListViewModel


const val SHARED_PREF_SETTINGS_NAME = "respect_settings3_"
const val TAG_TMP_DIR = "tmpDir"

val appKoinModule = module {
    single<Json> {
        Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        }
    }

    single<PhoneNumberUtil> {
        PhoneNumberUtil.createInstance(androidContext())
    }

    single<IPhoneNumberUtil> {
        IPhoneNumberUtilAndroid(phoneNumberUtil = get<PhoneNumberUtil>())
    }

    single<XXStringHasher> {
        XXStringHasherCommonJvm()
    }
    single<LaunchSendSmsUseCase> {
        LaunchSendSmsAndroid(androidContext())
    }
    single<LaunchSendEmailUseCase> {
        LaunchSendEmailAndroid(androidContext())
    }

    single<GetDeferredDeepLinkUseCase>(createdAtStart = true) {
        GetDeferredDeepLinkUseCaseAndroid(
            context = androidContext(),
            settings = get()
        )
    }

    single<LaunchShareLinkUseCase> {
        LaunchShareLinkAndroid(androidContext())
    }
    single<UidNumberMapper> {
        XXHashUidNumberMapper(xxStringHasher = get())
    }

    single<ConnectivityMonitor>(createdAtStart = true) {
        ConnectivityMonitorAndroid(androidContext())
    }

    single<OkHttpClient> {
        val cachePathProvider: CachePathsProvider = get()

        OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                }
            )
            .addInterceptor(
                UstadCacheInterceptor(
                    cache = get(),
                    tmpDirProvider = { File(cachePathProvider().tmpWorkPath.toString()) },
                    logger = NapierLoggingAdapter(),
                    json = get(),
                    connectivityMonitor = get(),
                )
            )
            .build()
    }

    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = get())
            }

            install(HttpRequestRetry)

            engine {
                preconfigured = get()
            }
        }
    }
    single<ExtractWebPageMetadataUseCase> {
        ExtractWebPageMetadataUseCaseAndroid(
            httpClient = get()
        )
    }

    BuildConfig.GEOLOCATION_API_ENDPOINT.takeIf { it.isNotEmpty() }?.also { geoIpEndpoint ->
        single<GetCountryForUrlUseCase> {
            GetCountryForUrlUseCaseImpl(
                httpClient = get(),
                geolocationEndpoint = Url(geoIpEndpoint),
            )
        }
    }

    viewModelOf(::OnboardingViewModel)
    viewModelOf(::AppsDetailViewModel)
    viewModelOf(::AppLauncherViewModel)
    viewModelOf(::EnterLinkViewModel)
    viewModelOf(::AppListViewModel)
    viewModelOf(::ClazzListViewModel)
    viewModelOf(::ClazzEditViewModel)
    viewModelOf(::ClazzDetailViewModel)
    viewModelOf(::OpdsFeedDetailViewModel)
    viewModelOf(::PublicationDetailViewModel)
    viewModelOf(::ReportViewModel)
    viewModelOf(::AcknowledgementViewModel)
    viewModelOf(::EnterInviteCodeViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::AcceptInviteViewModel)
    viewModelOf(::SignupViewModel)
    viewModelOf(::TermsAndConditionViewModel)
    viewModelOf(::WaitingForApprovalViewModel)
    viewModelOf(::CreateAccountViewModel)
    viewModelOf(::GetStartedViewModel)
    viewModelOf(::PasskeyListViewModel)
    viewModelOf(::HowPasskeyWorksViewModel)
    viewModelOf(::OtherOptionsViewModel)
    viewModelOf(::OtherOptionsSignupViewModel)
    viewModelOf(::EnterPasswordSignupViewModel)
    viewModelOf(::AccountListViewModel)
    viewModelOf(::ShareFeedbackViewModel)
    viewModelOf(::ManageAccountViewModel)
    viewModelOf(::PersonListViewModel)
    viewModelOf(::InvitePersonViewModel)
    viewModelOf(::CopyInviteCodeViewModel)
    viewModelOf(::PersonEditViewModel)
    viewModelOf(::PersonDetailViewModel)
    viewModelOf(::ReportDetailViewModel)
    viewModelOf(::ReportEditViewModel)
    viewModelOf(::ReportListViewModel)
    viewModelOf(::ReportTemplateListViewModel)
    viewModelOf(::IndicatorEditViewModel)
    viewModelOf(::ReportFilterEditViewModel)
    viewModelOf(::IndicatorListViewModel)
    viewModelOf(::IndicatorDetailViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ScanQRCodeViewModel)
    viewModelOf(::CreateAccountSetUserNameViewModel)
    viewModelOf(::ChangePasswordViewModel)
    viewModelOf(::SchoolDirectoryListViewModel)
    viewModelOf(::SchoolDirectoryEditViewModel)
    viewModelOf(::AssignmentListViewModel)
    viewModelOf(::AssignmentEditViewModel)
    viewModelOf(::AssignmentDetailViewModel)
    viewModelOf(::EnrollmentListViewModel)
    viewModelOf(::EnrollmentEditViewModel)
    viewModelOf(::InviteQrViewModel)
    viewModelOf(::CreateAccountSetPasswordViewModel)
    viewModelOf(::OpdsFeedListViewModel)
    viewModelOf(::OpdsFeedEditViewModel)
    viewModelOf(::OpdsFeedEditAddLinkViewModel)
    viewModelOf(::OpdsFeedShareViewModel)

    viewModelOf(::StatementListViewModel)
    viewModelOf(::StatementDetailViewModel)
    viewModelOf(::RawStatementViewModel)
    viewModelOf(::BookmarkListViewModel)
    viewModelOf(::SchoolSettingsViewModel)
    viewModelOf(::SharedDevicesSettingsViewmodel)
    viewModelOf(::TeacherPinConfirmationViewmodel)
    viewModelOf(::SelectClassViewModel)
    viewModelOf(::StudentListViewModel)

    single<LaunchSendWhatsAppUseCase> {
        LaunchSendWhatsAppUseCaseAndroid(androidContext())
    }

    single<OpenExternalLinkUseCase> {
        OpenExternalLinkUseCaseAndroid(androidContext())
    }

    single<GetOfflineStorageOptionsUseCase> {
        GetOfflineStorageOptionsUseCaseAndroid(
            getAndroidSdCardDirUseCase = get()
        )
    }

    single<GetAndroidSdCardDirUseCase> {
        GetAndroidSdCardDirUseCase(
            appContext = androidContext().applicationContext
        )
    }

    single<GetOfflineStorageSettingUseCase> {
        GetOfflineStorageSettingUseCase(
            getOfflineStorageOptionsUseCase = get(),
            settings = get(),
        )
    }

    single<CachePathsProvider> {
        CachePathsProviderAndroid(
            appContext = androidContext().applicationContext,
            getAndroidSdCardPathUseCase = get(),
            getOfflineStorageSettingUseCase = get(),
        )
    }

    single<Settings> {
        SharedPreferencesSettings(
            delegate = androidContext().getSharedPreferences(
                SHARED_PREF_SETTINGS_NAME,
                Context.MODE_PRIVATE
            )
        )
    }

    single<UstadCacheDb> {
        Room.databaseBuilder(
            androidContext().applicationContext,
            UstadCacheDb::class.java,
            UstadCacheBuilder.DEFAULT_DB_NAME
        ).addCallback(ClearNeighborsCallback())
            .addCacheDbMigrations()
            .build()
    }

    single<UstadCache> {
        UstadCacheBuilder(
            appContext = androidContext().applicationContext,
            storagePath = Path(
                File(androidContext().filesDir, "httpfiles").absolutePath
            ),
            sizeLimit = { 100_000_000L },
            db = get(),
        ).build()
    }

    single<OkHttpWebViewClient> {
        OkHttpWebViewClient(
            okHttpClient = get()
        )
    }
    single(named(TAG_TMP_DIR)) {
        File(androidContext().applicationContext.cacheDir, "tmp").apply { mkdirs() }
    }

    single<RespectAccountManager> {
        RespectAccountManager(
            settings = get(),
            json = get(),
            tokenManager = get(),
            appDataSource = get(),
        )
    }

    single<RespectTokenManager> {
        RespectTokenManager(
            settings = get(),
            json = get(),
        )
    }
    single<ValidateUsernameUseCase> {
        ValidateUsernameUseCase()
    }

    single<FilterUsernameUseCase> {
        FilterUsernameUseCase()
    }

    single<EncodeUserHandleUseCase> {
        EncodeUserHandleUseCaseImpl()
    }
    single {
        CreatePublicKeyCredentialRequestOptionsJsonUseCase()
    }

    single<GetCredentialUseCase> {
        GetCredentialUseCaseAndroidImpl(
            json = get(),
            createPublicKeyCredentialRequestOptionsJsonUseCase = get()
        )
    }
    single<VerifyDomainUseCase> {
        VerifyDomainUseCaseImpl(
            context = androidApplication()
        )
    }

    single<SavePasswordUseCase> {
        SavePasswordUseCaseAndroidImpl(
            enqueueActivityContextJobUseCase = get(),
        )
    }

    single<SchoolDirectoryDataSourceLocal> {
        SchoolDirectoryDataSourceDb(
            respectAppDb = get(),
            xxStringHasher = get()
        )
    }

    single<AddDirectoriesFromPropertiesUseCase>{
        AddDirectoriesFromPropertiesUseCase(
            xxStringHasher = get()
        )
    }

    single<RespectAppDatabase> {
        val appContext = androidContext().applicationContext
        Room.databaseBuilder<RespectAppDatabase>(
            appContext, appContext.getDatabasePath("respect_3_app.db").absolutePath
        ).setDriver(BundledSQLiteDriver())
            .addCallback(AddSchoolDirectoryCallback(addDirectoriesFromPropertiesUseCase = get()))
            .addCommonMigrations()
            .addMigrations(migrate6to8AddDirectories(addDirectoriesFromPropertiesUseCase = get()))
            .addMigrations(APP_MIGRATION_8_9_CLIENT)
            .build()
    }

    single<RespectAppDataSource> {
        RespectAppDataSourceRepository(
            local = RespectAppDataSourceDb(
                respectAppDatabase = get(),
                json = get(),
                xxStringHasher = get(),
            ),
            remote = RespectAppDataSourceHttp(
                local = RespectAppDataSourceDb(
                    respectAppDatabase = get(),
                    json = get(),
                    xxStringHasher = get(),
                ),
                httpClient = get(),
            )
        )
    }

    single<NavResultReturner> {
        NavResultReturnerImpl()
    }

    single<VerifyPasskeyUseCase> {
        VerifyPasskeyUseCase(
            httpClient = get(),
            json = get()
        )
    }
    single<XXHasher64Factory> {
        XXHasher64FactoryCommonJvm()
    }

    single<ExtendedDataSourceValidationHelper> {
        ExtendedDataSourceValidationHelperImpl(
            respectAppDb = get(),
            xxStringHasher = get(),
            xxHasher64Factory = get(),
        )
    }

    single<SetClipboardStringUseCase> {
        SetClipboardStringUseCaseAndroid(androidContext().applicationContext)
    }
    single<LaunchCustomTabUseCase> {
        LaunchCustomTabUseCaseAndroid(androidContext().applicationContext)
    }
    single<ShouldShowOnboardingUseCase> {
        ShouldShowOnboardingUseCase(settings = get())
    }

    single<GetUsageReportingEnabledUseCase> {
        GetUsageReportingEnabledUseCaseAndroid(androidContext())
    }

    single<SetUsageReportingEnabledUseCase> {
        SetUsageReportingEnabledUseCaseAndroid(androidContext())
    }

    single<GetDeviceInfoUseCase> {
        GetDeviceInfoUseCaseAndroid(androidContext())
    }

    single<GetDbFilesForE2EArtifactUploadUseCase> {
        GetDbFilesForE2EArtifactUploadUseCaseAndroid(
            context = androidContext(),
        )
    }

    single<E2EArtifactUploadUseCase> {
        E2EArtifactUploadUseCaseClient(
            httpClient = get(),
            getDbFilesForE2EArtifactUploadUseCase = get(),
            connectivityMonitor = get(),
        )
    }


    single<CreatePasskeyUseCaseAndroidChannelHost> {
        CreatePasskeyUseCaseAndroidChannelHost()
    }

    factory<LoadAaguidJsonUseCase> {
        LoadAaguidJsonUseCaseAndroid(
            appContext = androidContext().applicationContext,
            json = get(),
        )
    }

    factory<GetPasskeyProviderInfoUseCase> {
        GetPasskeyProviderInfoUseCaseImpl(
            json = get(),
            loadAaguidJsonUseCase = get()
        )
    }

    single<GetAppVersionInfoUseCase> {
        GetAppVersionInfoUseCaseAndroid(
            context = androidContext()
        )
    }

    single<GetWarningsUseCase> {
        GetWarningsUseCaseAndroid()
    }

    single<GetLicenseLabelUseCase> {
        GetLicenseLabelUseCaseAndroid(
            context = androidContext(),
            json = get(),
        )
    }

    single<EncryptPersonPasswordUseCase> {
        EncryptPersonPasswordUseCaseImpl()
    }

    single<ValidatePasswordUseCase> {
        ValidatePasswordUseCase()
    }

    single<SnackBarFlowDispatcher> {
        SnackBarFlowDispatcher()
    }

    single<SnackBarDispatcher> {
        get<SnackBarFlowDispatcher>()
    }

    single<ResolveUrlToNavCommandUseCase> {
        ResolveUrlToNavCommandUseCase()
    }

    single<InitDeepLinkUriProviderUseCaseAndroid> {
        InitDeepLinkUriProviderUseCaseAndroid()
    }

    single<InitDeepLinkUriProviderUseCase> {
        get<InitDeepLinkUriProviderUseCaseAndroid>()
    }

    single<PhoneNumValidatorUseCase> {
        PhoneNumValidatorAndroid(iPhoneNumberUtil = get())
    }

    single<OnClickPhoneNumUseCase> {
        OnClickPhoneNumberUseCaseAndroid(androidContext())
    }

    single<PinPublicationPrepareUseCase> {
        PinPublicationPrepareUseCase(
            httpClient = get(),
            db = get(),
            cache = get(),
            enqueueRunDownloadJobUseCase = get(),
            xml = get(),
        )
    }

    single<EnqueueRunDownloadJobUseCase> {
        EnqueueRunDownloadJobUseCaseAndroid(androidContext())
    }

    single<RunDownloadJobUseCase> {
        RunDownloadJobUseCaseImpl(
            okHttpClient = get(),
            db = get(),
            httpCache = get(),
        )
    }

    single<GetDevModeEnabledUseCase> {
        GetDevModeEnabledUseCase(settings = get())
    }

    single<SetDevModeEnabledUseCase> {
        SetDevModeEnabledUseCase(settings = get())
    }
    single<UrlToCustomDeepLinkUseCase> {
        UrlToCustomDeepLinkUseCase(customProtocol = androidApplication().packageName)
    }

    single<CustomDeepLinkToUrlUseCase> {
        CustomDeepLinkToUrlUseCase(customProtocol = androidApplication().packageName)
    }

    single {
        BiometricAuthUseCaseAndroidImpl()
    }

    single<BiometricAuthUseCase> {
        get<BiometricAuthUseCaseAndroidImpl>()
    }

    single<NavigateOnAppStartUseCase> {
        NavigateOnAppStartUseCase(
            accountManager = get(),
            initDeepLinkUriProvider = get(),
            getDeferredDeepLinkUseCase = get(),
            customDeepLinkToUrlUseCase = get(),
            resolveUrlToNavCommandUseCase = get(),
            settings = get(),
        )
    }
    single<EnableSharedDeviceModeUseCase> {
        EnableSharedDeviceModeUseCase(
            accountManager = get(),
            settings = get(),
        )
    }

    single<XapiNanoHttpdApp>(createdAtStart = true) {
        XapiNanoHttpdApp(
            port = 0,
            json = get(),
            xapiResourceProvider = get(),
        ).also { nanoHttpdApp ->
            GlobalScope.launch(Dispatchers.IO) {
                nanoHttpdApp.start()
                Napier.i("NanoHttpdXapi started")
            }
        }
    }

    single<XapiResourceProvider> {
        XapiResourceProviderAndroid()
    }

    single<GetXapiActivityForPublicationUseCase> {
        GetXapiActivityForPublicationUseCase(
            xml = get(),
            httpClient = get(),
        )
    }

    single<XML> {
        XML.v1 {
            recommended_1_0_0()
        }
    }

    single<EnqueueActivityContextJobUseCase> {
        EnqueueActivityContextJobUseCase()
    }

    single<GoToAppStoreUseCase> {
        GoToAppStoreUseCaseAndroid(
            appContext = androidApplication(),
        )
    }

    single<GetLanguageEndonymUseCase> {
        GetLanguageEndonymUseCase()
    }

    single<GetAndroidPackageIdForLaunchableAppUseCase> {
        GetAndroidPackageIdForLaunchableAppUseCase()
    }

    /**
     * The SchoolDirectoryEntry scope might be one instance per school url or one instance per account
     * per url.
     *
     * ScopeId is set as per SchoolDirectoryEntryScopeId
     *
     * If the upstream server provides a list of grants/permission rules then the school database
     * can be shared
     */
    scope<SchoolDirectoryEntry> {
        scoped<GetTokenAndUserProfileWithCredentialUseCase> {
            GetTokenAndUserProfileWithCredentialUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
                getDeviceInfoUseCase = get(),
            )
        }

        scoped<RespectSchoolPath> {
            RespectSchoolPath(
                path = Path(
                    File(
                        androidContext().filesDir,
                        SchoolDirectoryEntryScopeId.parse(id).schoolUrl.sanitizedForFilename()
                    ).absolutePath
                )
            )
        }

        scoped<SchoolDbPath> {
            SchoolDbPath.forSchoolUrl(SchoolDirectoryEntryScopeId.parse(id).schoolUrl)
        }

        scoped<RespectSchoolDatabase> {
            Room.databaseBuilder<RespectSchoolDatabase>(
                androidContext(),
                get<SchoolDbPath>().filename
            )
                .addCommonMigrations()
                .build()
        }

        scoped<SchoolPrimaryKeyGenerator> {
            SchoolPrimaryKeyGenerator(
                primaryKeyGenerator = PrimaryKeyGenerator(SchoolPrimaryKeyGenerator.TABLE_IDS)
            )
        }

        scoped<RedeemInviteUseCase> {
            RedeemInviteUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
                accountManager = get()
            )
        }
        scoped<GetInviteInfoUseCase> {
            GetInviteInfoUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                httpClient = get(),
            )
        }
        scoped<CreateInviteLinkUseCase> {
            CreateInviteLinkUseCase(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
            )
        }
        scoped<UsernameSuggestionUseCase> {
            UsernameSuggestionUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                httpClient = get(),
            )
        }

        scoped<CheckUsernameUniqueUseCase> {
            CheckUsernameUniqueUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                httpClient = get(),
            )
        }

        scoped<CreatePasskeyUseCase> {
            CreatePasskeyUseCaseAndroidImpl(
                sender = get(),
                json = get(),
                createPublicKeyJsonUseCase = get(),
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                uidNumberMapper = get(),
                getPasskeyProviderInfoUseCase = get(),
            )
        }

        scoped<CreatePublicKeyCredentialCreationOptionsJsonUseCase> {
            CreatePublicKeyCredentialCreationOptionsJsonUseCase(
                encodeUserHandleUseCase = get(),
                appName = {
                    "FFS" //getString(Res.string.app_name)
                },
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl
            )
        }

        scoped<CheckPasskeySupportUseCase> {
            CheckPasskeySupportUseCaseAndroidImpl(
                verifyDomainUseCase = get(),
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                respectAppDataSource = get(),
            )
        }

        scoped<AuthenticatePasswordUseCase> {
            AuthenticatePasswordUseCaseDbImpl(
                schoolDb = get(),
                encryptPersonPasswordUseCase = get(),
                uidNumberMapper = get(),
            )
        }
        scoped<ValidateQrCodeUseCase> {
            ValidateQrCodeUseCase(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl
            )
        }

        scoped<NavigateOnAccountCreatedUseCase> {
            NavigateOnAccountCreatedUseCase(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl
            )
        }
    }
    /**
     * ScopeId is set as per RespectAccountScopeId
     *
     * The RespectAccount scope will be linked to SchoolDirectoryEntry (the parent) scope.
     */
    scope<RespectAccount> {
        scoped<RespectAccountSchoolScopeLink> {
            val accountScopeId = RespectAccountScopeId.parse(id)
            val schoolDirectoryScope = SchoolDirectoryEntryScopeId(
                schoolUrl = accountScopeId.schoolUrl,
                accountPrincipalId = null,
            )

            linkTo(
                getKoin().getOrCreateScope<SchoolDirectoryEntry>(
                    schoolDirectoryScope.scopeId
                )
            )

            RespectAccountSchoolScopeLink(accountScopeId.schoolUrl)
        }
        scoped<AuthTokenProvider> {
            get<RespectTokenManager>().providerFor(id)
        }

        scoped<RemoteWriteQueue> {
            get<RespectAccountSchoolScopeLink>()
            val accountScopeId = RespectAccountScopeId.parse(id)

            RemoteWriteQueueDbImpl(
                schoolDb = get(),
                account = AuthenticatedUserPrincipalId(accountScopeId.accountPrincipalId.guid),
                enqueueDrainRemoteWriteQueueUseCase = get(),
            )
        }

        scoped<XapiRemoteWriteQueue> {
            get<RespectAccountSchoolScopeLink>()
            val accountScopeId = RespectAccountScopeId.parse(id)

            XapiRemoteWriteQueueDbImpl(
                schoolDb = get(),
                account = AuthenticatedUserPrincipalId(accountScopeId.accountPrincipalId.guid),
                enqueueDrainRemoteWriteQueueUseCase = get(),
            )
        }

        scoped<GetActivePersonPasskeysUseCase> {
            GetActivePersonPasskeysClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
            )
        }
        scoped<RevokePasskeyUseCase> {
            RevokePasskeyUseCaseClient(
                schoolUrl = SchoolDirectoryEntryScopeId.parse(id).schoolUrl,
                httpClient = get(),
            )
        }
        scoped<EnqueueDrainRemoteWriteQueueUseCase> {
            EnqueueDrainRemoteWriteQueueUseCaseAndroidImpl(
                context = androidContext().applicationContext,
                scopeId = id,
                scopeClass = RespectAccount::class,
            )
        }

        scoped<EnqueueDrainXapiRemoteWriteQueueUseCase> {
            EnqueueDrainXapiRemoteWriteQueueUseCaseAndroidImpl(
                context = androidContext().applicationContext,
                scopeId = id,
                scopeClass = RespectAccount::class,
            )
        }

        scoped<DrainRemoteWriteQueueUseCase> {
            DrainRemoteWriteQueueUseCase(
                remoteWriteQueue = get(),
                dataSource = get(),
            )
        }

        scoped<DrainXapiRemoteWriteQueueUseCase> {
            val repository = get<SchoolDataSource>() as SchoolDataSourceRepository
            DrainXapiRemoteWriteQueueUseCase(
                xapiRemoteWriteQueue = get(),
                remoteDataSource = repository.remote.xapiResource,
                localDataSource = repository.local.xapiResource,
            )
        }

        scoped<SchoolDataSourceLocal> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            SchoolDataSourceDb(
                schoolDb = get(),
                uidNumberMapper = get(),
                authenticatedUser = AuthenticatedUserPrincipalId(
                    accountScopeId.accountPrincipalId.guid
                ),
                checkPersonPermissionUseCase = get(),
                json = get(),
                defaultAppCatalogUrl = BuildConfig.RESPECT_DEFAULT_APP_LIST,
                schoolUrl = accountScopeId.schoolUrl,
            )
        }

        scoped<SchoolDataSource> {
            val schoolUrl = get<RespectAccountSchoolScopeLink>()
            val localDs = get<SchoolDataSourceLocal>()

            SchoolDataSourceRepository(
                local = localDs,
                remote = SchoolDataSourceHttpClient(
                    schoolUrl = schoolUrl.url,
                    schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                    httpClient = get(),
                    tokenProvider = get(),
                    validationHelper = get(),
                    json = get(),
                    defaultAppCatalogUrl = BuildConfig.RESPECT_DEFAULT_APP_LIST,
                    opdsFeedValidationHelper = localDs.opdsFeedDataSource,
                    opdsPublicationValidationHelper = localDs.opdsPublicationDataSource
                        .publicationNetworkValidationHelper
                ),
                validationHelper = get(),
                remoteWriteQueue = get(),
                xapiRemoteWriteQueue = get(),
                json = get(),
            )
        }

        scoped<SetSharedDevicePINUseCase> {
            SetSharedDevicePINUseCaseImpl(
                schoolDataSource = get(),
                respectAccountManager = get(),
                json = get(),
            )
        }
        scoped<GetSharedDevicePINUseCase> {
            GetSharedDevicePINUseCaseImpl(
                schoolDataSource = get(),
                respectAccountManager = get(),
                json = get(),
                setSharedDevicePINUseCase = get()
            )
        }
        scoped<GetSharedDeviceSelfSelectUseCase> {
            GetSharedDeviceSelfSelectUseCase(
                schoolDataSource = get(),
                respectAccountManager = get(),
                json = get(),
            )
        }
        scoped<SetSharedDeviceSelfSelectUseCase> {
            SetSharedDeviceSelfSelectUseCase(
                schoolDataSource = get(),
                respectAccountManager = get(),
                json = get(),
            )
        }
        scoped<ApproveOrDeclineInviteRequestUseCase> {
            ApproveOrDeclineInviteRequestUseCase(
                schoolDataSource = get(),
                updateClazzStudentXapiGroupUseCase = get(),
            )
        }

        scoped<AddChildAccountUseCase> {
            AddChildAccountUseCaseClient(
                schoolUrl = RespectAccountScopeId.parse(id).schoolUrl,
                authTokenProvider = get(),
                httpClient = get(),
                schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
                schoolDataSourceLocal = get(),
            )
        }

        scoped<GetAuthenticatedPersonUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)
            GetAuthenticatedPersonUseCase(
                authenticatedUserPrincipalId = AuthenticatedUserPrincipalId(
                    accountScopeId.accountPrincipalId.guid
                ),
                schoolDb = get(),
                uidNumberMapper = get(),
            )
        }

        scoped<CheckPersonPermissionUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            CheckPersonPermissionUseCaseDbImpl(
                schoolDb = get(),
                authenticatedUser = AuthenticatedUserPrincipalId(
                    accountScopeId.accountPrincipalId.guid
                ),
                uidNumberMapper = get(),
            )
        }

        scoped<CheckSchoolPermissionsUseCase> {
            CheckSchoolPermissionsUseCase(
                schoolDataSource = get(),
            )
        }

        scoped<PullSyncTracker> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            PullSyncTrackerDbImpl(
                schoolDb = get(),
                authenticatedUser = AuthenticatedUserPrincipalId(
                    accountScopeId.accountPrincipalId.guid
                ),
                uidNumberMapper = get(),
            )
        }

        scoped<EnqueueRunPullSyncUseCase> {
            EnqueueRunPullSyncUseCaseAndroidImpl(
                context = androidApplication(),
                scopeId = id,
                scopeClass = RespectAccount::class,
            )
        }

        scoped<RunPullSyncUseCase> {
            RunPullSyncUseCase(
                pullSyncTracker = get(),
                schoolDataSource = get(),
                authenticatedUser = RespectAccountScopeId.parse(id).accountPrincipalId,
            )
        }

        scoped<GetWritableRolesListUseCase> {
            GetWritableRolesListUseCaseImpl()
        }

        scoped<CreateClassUseCase> {
            CreateClassUseCase(dataSource = get())
        }

        scoped<LaunchAppUseCase> {
            LaunchAppUseCaseAndroid(
                appContext = androidContext().applicationContext,
                ustadCache = get(),
                getLaunchOptionsForPublicationUseCase = get(),
                getXapiLaunchParamsUseCase = get(),
                json = get(),
                opdsPublicationDataSource = get<SchoolDataSource>().opdsPublicationDataSource,
                getAndroidPackageIdForLaunchableAppUseCase = get(),
            )
        }

        scoped<GetLaunchOptionsForPublicationUseCase> {
            GetLaunchOptionsForPublicationUseCase(
                httpClient = get(),
                xml = get(),
                opdsPublicationDataSource = get<SchoolDataSource>().opdsPublicationDataSource,
            )
        }

        scoped<GetXapiLaunchParamsUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            GetXapiLaunchParamsUseCaseAndroid(
                nanoHttpdApp = get(),
                schoolUrl = accountScopeId.schoolUrl,
                authenticatedUser = accountScopeId.accountPrincipalId,
                accountManager = get(),
                uidNumberMapper = get(),
                schoolDb = get(),
            )
        }

        scoped<UpdateClazzStudentXapiGroupUseCase> {
            val accountScopeId = RespectAccountScopeId.parse(id)

            UpdateClazzStudentXapiGroupUseCase(
                schoolDataSource = get(),
                authenticatedUserPrincipalId = accountScopeId.accountPrincipalId,
                schoolUrl = accountScopeId.schoolUrl,
            )
        }

        scoped<AddBookmarkUseCase> {
             AddBookmarkUseCase(
                 schoolDataSource = get(),
             )
         }

         scoped<RemoveBookmarkUseCase> {
             RemoveBookmarkUseCase(
                 schoolDataSource = get(),
             )
         }

         scoped<GetPublicationForXapiActivityUseCase> {
             GetPublicationForXapiActivityUseCase(
                 opdsPublicationDataSource = get<SchoolDataSource>().opdsPublicationDataSource,
             )
         }

         scoped<MakePlaylistOpdsFeedUseCase> {
             val accountScopeId = RespectAccountScopeId.parse(id)
             MakePlaylistOpdsFeedUseCase(
                 schoolUrl = accountScopeId.schoolUrl,
                 schoolDirectoryEntryDataSource = get<RespectAppDataSource>().schoolDirectoryEntryDataSource,
             )
         }

        scoped<SaveOpdsFeedUseCase> {
            SaveOpdsFeedUseCase(
                xapiActivityProfileResource = get<SchoolDataSource>().xapiResource.activityProfile,
                opdsFeedDataSourceLocal = get<SchoolDataSourceLocal>().opdsFeedDataSource,
                json = get(),
            )
        }
    }
    single<RunReportUseCase> {
        MockRunReportUseCaseClientImpl()
    }
    single<ValidateEmailUseCase>{
        ValidateEmailUseCase()
    }
    single<CreateGraphFormatterUseCase> {
        CreateGraphFormatterUseCase()
    }
}
