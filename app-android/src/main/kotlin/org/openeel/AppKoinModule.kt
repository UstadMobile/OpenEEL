
package org.openeel

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.openeel.shared.domain.phonenumber.IPhoneNumberUtil
import org.openeel.shared.domain.phonenumber.IPhoneNumberUtilAndroid
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
import org.openeel.app.BuildConfig
import org.openeel.callback.AddDirectoriesFromPropertiesUseCase
import org.openeel.callback.AddSchoolDirectoryCallback
import org.openeel.callback.migrate6to8AddDirectories
import org.openeel.credentials.passkey.CheckPasskeySupportUseCase
import org.openeel.credentials.passkey.CheckPasskeySupportUseCaseAndroidImpl
import org.openeel.credentials.passkey.CreatePasskeyUseCase
import org.openeel.credentials.passkey.CreatePasskeyUseCaseAndroidChannelHost
import org.openeel.credentials.passkey.CreatePasskeyUseCaseAndroidImpl
import org.openeel.credentials.passkey.GetCredentialUseCase
import org.openeel.credentials.passkey.GetCredentialUseCaseAndroidImpl
import org.openeel.credentials.passkey.VerifyDomainUseCase
import org.openeel.credentials.passkey.VerifyDomainUseCaseImpl
import org.openeel.credentials.passkey.password.SavePasswordUseCase
import org.openeel.credentials.passkey.request.CreatePublicKeyCredentialCreationOptionsJsonUseCase
import org.openeel.credentials.passkey.request.CreatePublicKeyCredentialRequestOptionsJsonUseCase
import org.openeel.credentials.passkey.request.EncodeUserHandleUseCase
import org.openeel.credentials.passkey.request.GetPasskeyProviderInfoUseCase
import org.openeel.credentials.password.SavePasswordUseCaseAndroidImpl
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.RespectAppDataSource
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectAppDataSourceDb
import org.openeel.datalayer.db.RespectAppDatabase
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.SchoolDataSourceDb
import org.openeel.datalayer.db.addCommonMigrations
import org.openeel.datalayer.db.networkvalidation.ExtendedDataSourceValidationHelperImpl
import org.openeel.datalayer.db.school.GetAuthenticatedPersonUseCase
import org.openeel.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import org.openeel.datalayer.db.school.writequeue.RemoteWriteQueueDbImpl
import org.openeel.datalayer.db.school.xapi.writequeue.XapiRemoteWriteQueueDbImpl
import org.openeel.datalayer.db.schooldirectory.SchoolDirectoryDataSourceDb
import org.openeel.datalayer.db.shared.PullSyncTrackerDbImpl
import org.openeel.datalayer.http.RespectAppDataSourceHttp
import org.openeel.datalayer.http.SchoolDataSourceHttpClient
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.repository.RespectAppDataSourceRepository
import org.openeel.datalayer.repository.SchoolDataSourceRepository
import org.openeel.datalayer.repository.school.pullsync.EnqueueRunPullSyncUseCaseAndroidImpl
import org.openeel.datalayer.repository.school.pullsync.RunPullSyncUseCase
import org.openeel.datalayer.repository.school.writequeue.DrainRemoteWriteQueueUseCase
import org.openeel.datalayer.repository.school.writequeue.EnqueueDrainRemoteWriteQueueUseCaseAndroidImpl
import org.openeel.datalayer.repository.school.writequeue.EnqueueDrainXapiRemoteWriteQueueUseCaseAndroidImpl
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.domain.GetWritableRolesListUseCase
import org.openeel.datalayer.school.domain.GetWritableRolesListUseCaseImpl
import org.openeel.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import org.openeel.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase
import org.openeel.datalayer.school.writequeue.EnqueueRunPullSyncUseCase
import org.openeel.datalayer.school.writequeue.RemoteWriteQueue
import org.openeel.lib.xapi.remotewritequeue.DrainXapiRemoteWriteQueueUseCase
import org.openeel.lib.xapi.remotewritequeue.EnqueueDrainXapiRemoteWriteQueueUseCase
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.shared.pullsync.PullSyncTracker
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.lib.primarykeygen.PrimaryKeyGenerator
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.nanohttpd.XapiNanoHttpdApp
import org.openeel.libutil.ext.sanitizedForFilename
import org.openeel.libxxhash.XXHasher64Factory
import org.openeel.libxxhash.XXStringHasher
import org.openeel.libxxhash.jvmimpl.XXHasher64FactoryCommonJvm
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import org.openeel.shared.domain.account.RespectAccount
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.account.RespectAccountSchoolScopeLink
import org.openeel.shared.domain.account.authwithopenid.OpenIdAuthorizationUseCase
import org.openeel.shared.domain.account.authwithopenid.GetTokenAndUserProfileWithOpenIdUseCase
import org.openeel.shared.domain.account.RespectTokenManager
import org.openeel.shared.domain.account.child.AddChildAccountUseCase
import org.openeel.shared.domain.account.authenticatepassword.AuthenticatePasswordUseCase
import org.openeel.shared.domain.account.child.AddChildAccountUseCaseClient
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCaseClient
import org.openeel.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import org.openeel.shared.domain.account.invite.GetInviteInfoUseCase
import org.openeel.shared.domain.account.invite.GetInviteInfoUseCaseClient
import org.openeel.shared.domain.account.invite.RedeemInviteUseCase
import org.openeel.shared.domain.account.invite.RedeemInviteUseCaseClient
import org.openeel.shared.domain.navigation.onaccountcreated.NavigateOnAccountCreatedUseCase
import org.openeel.shared.domain.account.passkey.EncodeUserHandleUseCaseImpl
import org.openeel.shared.domain.account.passkey.GetPasskeyProviderInfoUseCaseImpl
import org.openeel.shared.domain.account.passkey.GetActivePersonPasskeysClient
import org.openeel.shared.domain.account.passkey.GetActivePersonPasskeysUseCase
import org.openeel.shared.domain.account.passkey.LoadAaguidJsonUseCase
import org.openeel.shared.domain.account.passkey.LoadAaguidJsonUseCaseAndroid
import org.openeel.shared.domain.account.passkey.RevokePasskeyUseCase
import org.openeel.shared.domain.account.passkey.RevokePasskeyUseCaseClient
import org.openeel.shared.domain.account.passkey.VerifyPasskeyUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCaseImpl
import org.openeel.shared.domain.account.username.UsernameSuggestionUseCase
import org.openeel.shared.domain.account.username.UsernameSuggestionUseCaseClient
import org.openeel.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import org.openeel.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCaseClient
import org.openeel.shared.domain.account.username.filterusername.FilterUsernameUseCase
import org.openeel.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import org.openeel.shared.domain.account.validatepassword.ValidatePasswordUseCase
import org.openeel.shared.domain.account.validateqrbadge.ValidateQrCodeUseCase
import org.openeel.shared.domain.appversioninfo.GetAppVersionInfoUseCase
import org.openeel.shared.domain.appversioninfo.GetAppVersionInfoUseCaseAndroid
import org.openeel.shared.domain.clipboard.SetClipboardStringUseCase
import org.openeel.shared.domain.clipboard.SetClipboardStringUseCaseAndroid
import org.openeel.shared.domain.geolookup.GetCountryForUrlUseCase
import org.openeel.shared.domain.geolookup.GetCountryForUrlUseCaseImpl
import org.openeel.shared.domain.createlink.CreateInviteLinkUseCase
import org.openeel.shared.domain.devmode.GetDevModeEnabledUseCase
import org.openeel.shared.domain.devmode.SetDevModeEnabledUseCase
import org.openeel.shared.domain.school.LaunchCustomTabUseCaseAndroid
import org.openeel.app.domain.e2eartifactupload.GetDbFilesForE2EArtifactUploadUseCaseAndroid
import org.openeel.datalayer.db.APP_MIGRATION_8_9_CLIENT
import org.openeel.shared.domain.activitycontextjobprocessor.EnqueueActivityContextJobUseCase
import org.openeel.OpenIdAuthorizationUseCaseAndroid
import org.openeel.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import org.openeel.shared.domain.getdeviceinfo.GetDeviceInfoUseCaseAndroid
import org.openeel.shared.domain.e2eartifactupload.GetDbFilesForE2EArtifactUploadUseCase
import org.openeel.shared.domain.e2eartifactupload.E2EArtifactUploadUseCase
import org.openeel.shared.domain.e2eartifactupload.E2EArtifactUploadUseCaseClient
import org.openeel.shared.domain.getwarnings.GetWarningsUseCase
import org.openeel.shared.domain.getwarnings.GetWarningsUseCaseAndroid
import org.openeel.shared.domain.license.GetLicenseLabelUseCaseAndroid
import org.openeel.shared.domain.bookmark.AddBookmarkUseCase
import org.openeel.shared.domain.bookmark.RemoveBookmarkUseCase
import org.openeel.shared.domain.launchapp.LaunchAppUseCase
import org.openeel.shared.domain.launchapp.LaunchAppUseCaseAndroid
import org.openeel.shared.domain.launchers.LaunchSendWhatsAppUseCase
import org.openeel.shared.domain.launchers.LaunchSendWhatsAppUseCaseAndroid
import org.openeel.shared.domain.openexternallink.OpenExternalLinkUseCase
import org.openeel.shared.domain.launchers.OpenExternalLinkUseCaseAndroid
import org.openeel.shared.domain.externallink.ExtractWebPageMetadataUseCase
import org.openeel.shared.domain.externallink.ExtractWebPageMetadataUseCaseAndroid
import org.openeel.shared.domain.navigation.deeplink.CustomDeepLinkToUrlUseCase
import org.openeel.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase
import org.openeel.shared.domain.onboarding.ShouldShowOnboardingUseCase
import org.openeel.shared.domain.permissions.CheckSchoolPermissionsUseCase
import org.openeel.shared.domain.phonenumber.OnClickPhoneNumUseCase
import org.openeel.shared.domain.phonenumber.OnClickPhoneNumberUseCaseAndroid
import org.openeel.shared.domain.phonenumber.PhoneNumValidatorAndroid
import org.openeel.shared.domain.phonenumber.PhoneNumValidatorUseCase
import org.openeel.shared.domain.report.formatter.CreateGraphFormatterUseCase
import org.openeel.shared.domain.report.query.MockRunReportUseCaseClientImpl
import org.openeel.shared.domain.report.query.RunReportUseCase
import org.openeel.shared.domain.school.LaunchCustomTabUseCase
import org.openeel.shared.domain.school.RespectSchoolPath
import org.openeel.shared.domain.school.SchoolDbPath
import org.openeel.shared.domain.school.SchoolPrimaryKeyGenerator
import org.openeel.shared.domain.storage.CachePathsProviderAndroid
import org.openeel.shared.domain.storage.GetAndroidSdCardDirUseCase
import org.openeel.shared.domain.storage.GetOfflineStorageOptionsUseCaseAndroid
import org.openeel.shared.domain.storage.GetOfflineStorageSettingUseCase
import org.openeel.shared.domain.usagereporting.GetUsageReportingEnabledUseCase
import org.openeel.shared.domain.usagereporting.GetUsageReportingEnabledUseCaseAndroid
import org.openeel.shared.domain.usagereporting.SetUsageReportingEnabledUseCase
import org.openeel.shared.domain.usagereporting.SetUsageReportingEnabledUseCaseAndroid
import org.openeel.shared.domain.validateemail.ValidateEmailUseCase
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.NavResultReturnerImpl
import org.openeel.shared.util.di.RespectAccountScopeId
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import org.openeel.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.app.appstate.SnackBarFlowDispatcher
import org.openeel.shared.viewmodel.apps.detail.AppsDetailViewModel
import org.openeel.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import org.openeel.shared.viewmodel.apps.launcher.AppLauncherViewModel
import org.openeel.shared.viewmodel.apps.list.AppListViewModel
import org.openeel.shared.viewmodel.assignment.detail.AssignmentDetailViewModel
import org.openeel.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import org.openeel.shared.viewmodel.assignment.list.AssignmentListViewModel
import org.openeel.shared.viewmodel.enrollment.list.EnrollmentListViewModel
import org.openeel.shared.viewmodel.enrollment.edit.EnrollmentEditViewModel
import org.openeel.shared.viewmodel.clazz.detail.ClazzDetailViewModel
import org.openeel.shared.viewmodel.clazz.edit.ClazzEditViewModel
import org.openeel.shared.viewmodel.clazz.list.ClazzListViewModel
import org.openeel.shared.viewmodel.catalog.publicationdetail.PublicationDetailViewModel
import org.openeel.shared.viewmodel.catalog.opdsfeeddetail.OpdsFeedDetailViewModel
import org.openeel.shared.viewmodel.manageuser.accountlist.AccountListViewModel
import org.openeel.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel
import org.openeel.shared.viewmodel.manageuser.acceptinvite.AcceptInviteViewModel
import org.openeel.shared.viewmodel.manageuser.enterpasswordsignup.EnterPasswordSignupViewModel
import org.openeel.shared.viewmodel.manageuser.getstarted.GetStartedViewModel
import org.openeel.shared.viewmodel.manageuser.howpasskeywork.HowPasskeyWorksViewModel
import org.openeel.shared.viewmodel.manageuser.enterinvitecode.EnterInviteCodeViewModel
import org.openeel.shared.viewmodel.manageuser.login.LoginViewModel
import org.openeel.shared.viewmodel.manageuser.otheroption.OtherOptionsViewModel
import org.openeel.shared.viewmodel.manageuser.otheroptionsignup.OtherOptionsSignupViewModel
import org.openeel.shared.viewmodel.manageuser.profile.SignupViewModel
import org.openeel.shared.viewmodel.manageuser.signup.CreateAccountViewModel
import org.openeel.shared.viewmodel.manageuser.termsandcondition.TermsAndConditionViewModel
import org.openeel.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel
import org.openeel.shared.viewmodel.onboarding.OnboardingViewModel
import org.openeel.shared.viewmodel.person.changepassword.ChangePasswordViewModel
import org.openeel.shared.viewmodel.person.copycode.CopyInviteCodeViewModel
import org.openeel.shared.viewmodel.person.detail.PersonDetailViewModel
import org.openeel.shared.domain.biometric.BiometricAuthUseCase
import org.openeel.shared.domain.biometric.BiometricAuthUseCaseAndroidImpl
import org.openeel.shared.domain.catalog.saveopdsfeed.SaveOpdsFeedUseCase
import org.openeel.shared.domain.createclass.CreateClassUseCase
import org.openeel.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase
import org.openeel.shared.domain.geticonforxapiactivity.GetPublicationForXapiActivityUseCase
import org.openeel.shared.domain.getlanguageendonym.GetLanguageEndonymUseCase
import org.openeel.shared.domain.launchapp.getlaunchoptionsforpublication.GetLaunchOptionsForPublicationUseCase
import org.openeel.shared.domain.launchapp.getxapilaunchparams.GetXapiLaunchParamsUseCase
import org.openeel.shared.domain.launchapp.getxapilaunchparams.GetXapiLaunchParamsUseCaseAndroid
import org.openeel.shared.domain.launchapp.gotoappstore.GoToAppStoreUseCase
import org.openeel.shared.domain.launchapp.gotoappstore.GoToAppStoreUseCaseAndroid
import org.openeel.shared.domain.license.GetLicenseLabelUseCase
import org.openeel.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCase
import org.openeel.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCase
import org.openeel.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCaseAndroid
import org.openeel.shared.viewmodel.person.edit.PersonEditViewModel
import org.openeel.shared.viewmodel.person.list.PersonListViewModel
import org.openeel.shared.viewmodel.person.inviteperson.InvitePersonViewModel
import org.openeel.shared.viewmodel.person.qrcode.InviteQrViewModel
import org.openeel.shared.viewmodel.person.manageaccount.ManageAccountViewModel
import org.openeel.shared.viewmodel.person.passkeylist.PasskeyListViewModel
import org.openeel.shared.viewmodel.report.ReportViewModel
import org.openeel.shared.viewmodel.report.detail.ReportDetailViewModel
import org.openeel.shared.viewmodel.report.edit.ReportEditViewModel
import org.openeel.shared.viewmodel.report.filteredit.ReportFilterEditViewModel
import org.openeel.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel
import org.openeel.shared.viewmodel.report.indictor.edit.IndicatorEditViewModel
import org.openeel.shared.viewmodel.report.indictor.list.IndicatorListViewModel
import org.openeel.shared.viewmodel.report.list.ReportListViewModel
import org.openeel.shared.viewmodel.report.list.ReportTemplateListViewModel
import org.openeel.sharedse.domain.account.authenticatepassword.AuthenticatePasswordUseCaseDbImpl
import java.io.File
import org.openeel.shared.viewmodel.settings.SettingsViewModel
import org.openeel.shared.viewmodel.person.setusernameandpassword.CreateAccountSetPasswordViewModel
import org.openeel.shared.viewmodel.person.setusernameandpassword.CreateAccountSetUserNameViewModel
import org.openeel.shared.viewmodel.catalog.opdsfeededit.OpdsFeedEditViewModel
import org.openeel.shared.viewmodel.catalog.opdsfeededitaddlink.OpdsFeedEditAddLinkViewModel
import org.openeel.shared.viewmodel.catalog.opdsfeedlist.OpdsFeedListViewModel
import org.openeel.shared.viewmodel.catalog.opdsfeedshare.OpdsFeedShareViewModel
import org.openeel.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditViewModel
import org.openeel.shared.viewmodel.schooldirectory.list.SchoolDirectoryListViewModel
import org.openeel.shared.domain.sharelink.LaunchSendEmailUseCase
import org.openeel.shared.domain.sharelink.LaunchShareLinkUseCase
import org.openeel.shared.domain.sharelink.LaunchSendSmsUseCase
import org.openeel.shared.domain.sendinvite.LaunchSendSmsAndroid
import org.openeel.shared.domain.sendinvite.LaunchSendEmailAndroid
import org.openeel.shared.domain.sendinvite.LaunchShareLinkAndroid
import org.openeel.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import org.openeel.shared.viewmodel.scanqrcode.ScanQRCodeViewModel
import org.openeel.shared.domain.navigation.deferreddeeplink.GetDeferredDeepLinkUseCaseAndroid
import org.openeel.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import org.openeel.shared.domain.opds.getxapiactivityid.GetXapiActivityForPublicationUseCase
import org.openeel.shared.viewmodel.statement.detail.RawStatementViewModel
import org.openeel.shared.viewmodel.statement.detail.StatementDetailViewModel
import org.openeel.shared.viewmodel.statement.list.StatementListViewModel
import org.openeel.shared.domain.xapi.xapinanohttpd.XapiResourceProviderAndroid
import org.openeel.shared.viewmodel.catalog.bookmark.BookmarkListViewModel


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

    single {
        OpenIdAuthorizationUseCaseAndroid(
            enqueueActivityContextJobUseCase = get(),
        )
    }

    single<OpenIdAuthorizationUseCase> {
        get<OpenIdAuthorizationUseCaseAndroid>()
    }

    single {
        GetTokenAndUserProfileWithOpenIdUseCase(
            httpClient = get(),
        )
    }

    single {
        HandleOpenIdAuthorizationResultUseCaseAndroid(
            context = androidContext().applicationContext,
            getTokenAndUserProfileWithOpenIdUseCase = get(),
        )
    }

    single<GoToAppStoreUseCase> {
        GoToAppStoreUseCaseAndroid(
            appContext = androidApplication(),
        )
    }

    single<GetLanguageEndonymUseCase> {
        GetLanguageEndonymUseCase()
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
