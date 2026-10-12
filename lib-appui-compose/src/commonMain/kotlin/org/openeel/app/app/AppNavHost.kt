package org.openeel.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.openeel.app.view.acknowledgement.AcknowledgementScreen
import org.openeel.app.view.apps.detail.AppsDetailScreen
import org.openeel.app.view.apps.enterlink.EnterLinkScreen
import org.openeel.app.view.apps.list.AppListScreen
import org.openeel.app.view.assignment.detail.AssignmentDetailScreen
import org.openeel.app.view.assignment.edit.AssignmentEditScreen
import org.openeel.app.view.assignment.list.AssignmentListScreen
import org.openeel.app.view.catalog.bookmark.BookmarkListScreen
import org.openeel.app.view.clazz.detail.ClazzDetailScreen
import org.openeel.app.view.clazz.edit.ClazzEditScreen
import org.openeel.app.view.clazz.list.ClazzListScreen
import org.openeel.app.view.enrollment.edit.EnrollmentEditScreen
import org.openeel.app.view.enrollment.list.EnrollmentListScreen
import org.openeel.app.view.home.HomeScreen
import org.openeel.app.view.catalog.publicationdetail.PublicationDetailScreen
import org.openeel.app.view.catalog.opdsfeeddetail.OpdsFeedDetailScreen
import org.openeel.app.view.manageuser.accountlist.AccountListScreen
import org.openeel.app.view.manageuser.acceptinvite.AcceptInviteScreen
import org.openeel.app.view.manageuser.createaccount.CreateAccountScreen
import org.openeel.app.view.manageuser.enterpasswordsignup.EnterPasswordSignupScreen
import org.openeel.app.view.manageuser.getstarted.GetStartedScreen
import org.openeel.app.view.manageuser.howpasskeywork.HowPasskeyWorksScreen
import org.openeel.app.view.manageuser.enterinvitecode.EnterInviteCodeScreen
import org.openeel.app.view.manageuser.login.LoginScreen
import org.openeel.app.view.manageuser.otheroption.OtherOptionsScreen
import org.openeel.app.view.manageuser.otheroptionsignup.OtherOptionsSignupScreen
import org.openeel.app.view.manageuser.sharefeedback.ShareFeedbackScreen
import org.openeel.app.view.manageuser.signup.SignupScreen
import org.openeel.app.view.manageuser.termsandcondition.TermsAndConditionScreen
import org.openeel.app.view.manageuser.waitingforapproval.WaitingForApprovalScreen
import org.openeel.app.view.onboarding.OnboardingScreen
import org.openeel.app.view.person.changepassword.ChangePasswordScreen
import org.openeel.app.view.person.copycode.CopyInviteCodeScreen
import org.openeel.app.view.person.detail.PersonDetailScreen
import org.openeel.app.view.person.edit.PersonEditScreen
import org.openeel.app.view.person.inviteperson.InvitePersonScreen
import org.openeel.app.view.person.list.PersonListScreen
import org.openeel.app.view.person.manageaccount.ManageAccountScreen
import org.openeel.app.view.person.passkeyList.PasskeyListScreen
import org.openeel.app.view.person.qrcode.InviteQrScreen
import org.openeel.app.view.person.setusernameandpassword.CreateAccountSetPasswordScreen
import org.openeel.app.view.person.setusernameandpassword.CreateAccountSetUsernameScreen
import org.openeel.app.view.catalog.opdsfeededitaddlink.OpdsFeedEditAddLinkScreen
import org.openeel.app.view.catalog.opdsfeededit.OpdsFeedEditScreen
import org.openeel.app.view.catalog.opdsfeedlist.OpdsFeedListScreen
import org.openeel.app.view.catalog.opdsfeedshare.PlaylistShareScreen
import org.openeel.app.view.report.detail.ReportDetailScreen
import org.openeel.app.view.report.edit.ReportEditScreen
import org.openeel.app.view.report.filteredit.ReportFilterEditScreen
import org.openeel.app.view.report.indicator.detail.IndicatorDetailScreen
import org.openeel.app.view.report.indicator.edit.IndictorEditScreen
import org.openeel.app.view.report.indicator.list.IndicatorListScreen
import org.openeel.app.view.report.list.ReportListScreen
import org.openeel.app.view.report.list.ReportTemplateListScreen
import org.openeel.app.view.scanqrcode.ScanQRCodeScreen
import org.openeel.app.view.schooldirectory.edit.SchoolDirectoryEditScreen
import org.openeel.app.view.schooldirectory.list.SchoolDirectoryListScreen
import org.openeel.app.view.settings.SettingsScreen
import org.openeel.app.view.statement.detail.RawStatementScreen
import org.openeel.app.view.statement.detail.StatementDetailScreen
import org.openeel.app.view.statement.list.StatementListScreen
import org.openeel.app.viewmodel.openEelViewModel
import org.openeel.shared.navigation.AccountList
import org.openeel.shared.navigation.Acknowledgement
import org.openeel.shared.navigation.AppsDetail
import org.openeel.shared.navigation.AssignmentDetail
import org.openeel.shared.navigation.AssignmentEdit
import org.openeel.shared.navigation.AssignmentList
import org.openeel.shared.navigation.ChangePassword
import org.openeel.shared.navigation.ClazzDetail
import org.openeel.shared.navigation.ClazzEdit
import org.openeel.shared.navigation.ClazzList
import org.openeel.shared.navigation.AcceptInvite
import org.openeel.shared.navigation.BookmarkList
import org.openeel.shared.navigation.CopyCode
import org.openeel.shared.navigation.CreateAccount
import org.openeel.shared.navigation.CreateAccountSetPassword
import org.openeel.shared.navigation.CreateAccountSetUsername
import org.openeel.shared.navigation.EnrollmentEdit
import org.openeel.shared.navigation.EnrollmentList
import org.openeel.shared.navigation.EnterLink
import org.openeel.shared.navigation.EnterPasswordSignup
import org.openeel.shared.navigation.GetStartedScreen
import org.openeel.shared.navigation.HowPasskeyWorks
import org.openeel.shared.navigation.IndicatorDetail
import org.openeel.shared.navigation.IndicatorList
import org.openeel.shared.navigation.IndictorEdit
import org.openeel.shared.navigation.InvitePerson
import org.openeel.shared.navigation.EnterInviteCode
import org.openeel.shared.navigation.ExternalLinkEdit
import org.openeel.shared.navigation.Home
import org.openeel.shared.navigation.PublicationDetail
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.navigation.LoginScreen
import org.openeel.shared.navigation.ManageAccount
import org.openeel.shared.navigation.Onboarding
import org.openeel.shared.navigation.OtherOption
import org.openeel.shared.navigation.OtherOptionsSignup
import org.openeel.shared.navigation.PasskeyList
import org.openeel.shared.navigation.PersonDetail
import org.openeel.shared.navigation.PersonEdit
import org.openeel.shared.navigation.PersonList
import org.openeel.shared.navigation.OpdsFeedEdit
import org.openeel.shared.navigation.PlaylistList
import org.openeel.shared.navigation.PlaylistShare
import org.openeel.shared.navigation.QrCode
import org.openeel.shared.navigation.RawStatement
import org.openeel.shared.navigation.Report
import org.openeel.shared.navigation.ReportDetail
import org.openeel.shared.navigation.ReportEdit
import org.openeel.shared.navigation.ReportEditFilter
import org.openeel.shared.navigation.ReportTemplateList
import org.openeel.shared.navigation.OpenEelAppLauncher
import org.openeel.shared.navigation.OpenEelAppList
import org.openeel.shared.navigation.OpenEelComposeNavController
import org.openeel.shared.navigation.ScanQRCode
import org.openeel.shared.navigation.SchoolDirectoryEdit
import org.openeel.shared.navigation.SchoolDirectoryList
import org.openeel.shared.navigation.Settings
import org.openeel.shared.navigation.ShareFeedback
import org.openeel.shared.navigation.SignupScreen
import org.openeel.shared.navigation.StatementDetail
import org.openeel.shared.navigation.StatementList
import org.openeel.shared.navigation.TermsAndCondition
import org.openeel.shared.navigation.WaitingForApproval
import org.openeel.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.apps.detail.AppsDetailViewModel
import org.openeel.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import org.openeel.shared.viewmodel.apps.list.AppListViewModel
import org.openeel.shared.viewmodel.clazz.detail.ClazzDetailViewModel
import org.openeel.shared.viewmodel.clazz.edit.ClazzEditViewModel
import org.openeel.shared.viewmodel.clazz.list.ClazzListViewModel
import org.openeel.shared.viewmodel.enrollment.edit.EnrollmentEditViewModel
import org.openeel.shared.viewmodel.enrollment.list.EnrollmentListViewModel
import org.openeel.shared.viewmodel.catalog.publicationdetail.PublicationDetailViewModel
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
import org.openeel.shared.viewmodel.report.detail.ReportDetailViewModel
import org.openeel.shared.viewmodel.report.edit.ReportEditViewModel
import org.openeel.shared.viewmodel.report.filteredit.ReportFilterEditViewModel
import org.openeel.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel
import org.openeel.shared.viewmodel.report.indictor.edit.IndicatorEditViewModel
import org.openeel.shared.viewmodel.report.indictor.list.IndicatorListViewModel
import org.openeel.shared.viewmodel.report.list.ReportListViewModel
import org.openeel.shared.viewmodel.report.list.ReportTemplateListViewModel
import org.openeel.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditViewModel
import org.openeel.shared.viewmodel.schooldirectory.list.SchoolDirectoryListViewModel
import org.openeel.shared.viewmodel.settings.SettingsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    respectNavController: OpenEelComposeNavController = remember(Unit) {
        OpenEelComposeNavController(navController)
    },
    onSetAppUiState: (AppUiState) -> Unit,
    modifier: Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Acknowledgement(),
        modifier = modifier,
    ) {
        composable<Acknowledgement> {
            val viewModel: AcknowledgementViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AcknowledgementScreen(viewModel)
        }

        composable<Onboarding> {
            val viewModel: OnboardingViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OnboardingScreen(viewModel)
        }

        composable<LoginScreen> {
            val viewModel: LoginViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LoginScreen(viewModel)
        }

        composable<EnterInviteCode> {
            val viewModel: EnterInviteCodeViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterInviteCodeScreen(viewModel)
        }

        composable<OpenEelAppLauncher> {
            HomeScreen(
                respectNavController = respectNavController,
                onSetAppUiState = onSetAppUiState,
            )
        }

        composable<Home> {
            HomeScreen(
                respectNavController = respectNavController,
                onSetAppUiState = onSetAppUiState,
            )
        }

        composable<AppsDetail> {
            val viewModel: AppsDetailViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AppsDetailScreen(viewModel = viewModel)
        }

        composable<AssignmentList> {
            AssignmentListScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<AssignmentEdit> {
            AssignmentEditScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<AssignmentDetail> {
            AssignmentDetailScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<StatementList>{
            StatementListScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<StatementDetail> {
            StatementDetailScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<RawStatement> {
            RawStatementScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<BookmarkList> {
            BookmarkListScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<ClazzList> {
            val viewModel: ClazzListViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzListScreen(viewModel = viewModel)
        }

        composable<ClazzEdit> {
            val viewModel: ClazzEditViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzEditScreen(viewModel = viewModel)
        }

        composable<ClazzDetail> {
            val viewModel: ClazzDetailViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzDetailScreen(viewModel = viewModel)
        }

        composable<EnrollmentList> {
            val viewModel: EnrollmentListViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnrollmentListScreen(viewModel = viewModel)
        }

        composable<EnrollmentEdit> {
            val viewModel: EnrollmentEditViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnrollmentEditScreen(viewModel = viewModel)
        }

        composable<ReportDetail> {
            val viewModel: ReportDetailViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportDetailScreen(navController = navController, viewModel = viewModel)
        }

        composable<ReportEdit> {
            val viewModel: ReportEditViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportEditScreen(viewModel = viewModel)
        }

        composable<Report> {
            val viewModel: ReportListViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportListScreen(viewModel = viewModel)
        }

        composable<ReportTemplateList> {
            val viewModel: ReportTemplateListViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportTemplateListScreen(viewModel = viewModel)
        }

        composable<IndictorEdit> {
            val viewModel: IndicatorEditViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndictorEditScreen(viewModel = viewModel)
        }

        composable<ReportEditFilter> {
            val viewModel: ReportFilterEditViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportFilterEditScreen(navController = navController, viewModel = viewModel)
        }

        composable<IndicatorList> {
            val viewModel: IndicatorListViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndicatorListScreen(viewModel = viewModel)
        }

        composable<IndicatorDetail> {
            val viewModel: IndicatorDetailViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndicatorDetailScreen(navController = navController, viewModel = viewModel)
        }

        composable<HowPasskeyWorks> {
            val viewModel: HowPasskeyWorksViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            HowPasskeyWorksScreen(viewModel = viewModel)
        }

        composable<OpenEelAppList> {
            val viewModel: AppListViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AppListScreen(viewModel = viewModel)
        }

        composable<EnterLink> {
            val viewModel: EnterLinkViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterLinkScreen(viewModel = viewModel)
        }

        composable<GetStartedScreen> {
            val viewModel: GetStartedViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            GetStartedScreen(viewModel = viewModel)
        }

        composable<OtherOption> {
            val viewModel: OtherOptionsViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OtherOptionsScreen(viewModel = viewModel)
        }

        composable<OpdsFeedDetail> {
            OpdsFeedDetailScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<OtherOptionsSignup> {
            val viewModel: OtherOptionsSignupViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OtherOptionsSignupScreen(viewModel = viewModel)
        }

        composable<EnterPasswordSignup> {
            val viewModel: EnterPasswordSignupViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterPasswordSignupScreen(viewModel = viewModel)
        }

        composable<PublicationDetail> {
            val viewModel: PublicationDetailViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            PublicationDetailScreen(viewModel = viewModel)
        }

        composable<SignupScreen> {
            val viewModel: SignupViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SignupScreen(viewModel = viewModel)
        }

        composable<AcceptInvite> {
            val viewModel: AcceptInviteViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AcceptInviteScreen(viewModel = viewModel)
        }

        composable<TermsAndCondition> {
            val viewModel: TermsAndConditionViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            TermsAndConditionScreen(viewModel = viewModel)
        }

        composable<CreateAccount> {
            val viewModel: CreateAccountViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            CreateAccountScreen(viewModel = viewModel)
        }

        composable<WaitingForApproval> {
            val viewModel: WaitingForApprovalViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            WaitingForApprovalScreen(viewModel = viewModel)
        }

        composable<AccountList> {
            AccountListScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<ShareFeedback> {
            ShareFeedbackScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonList> {
            PersonListScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonDetail> {
            PersonDetailScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<ManageAccount> {
            ManageAccountScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PasskeyList> {
            PasskeyListScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonEdit> {
            PersonEditScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<Settings> {
            val viewModel: SettingsViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SettingsScreen(viewModel = viewModel)
        }

        composable<ScanQRCode> {
            ScanQRCodeScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PlaylistList> {
            OpdsFeedListScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<OpdsFeedEdit> {
            OpdsFeedEditScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<PlaylistShare> {
            PlaylistShareScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<ExternalLinkEdit> {
            OpdsFeedEditAddLinkScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<SchoolDirectoryList> {
            val viewModel: SchoolDirectoryListViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SchoolDirectoryListScreen(viewModel)
        }

        composable<SchoolDirectoryEdit> {
            val viewModel: SchoolDirectoryEditViewModel = openEelViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SchoolDirectoryEditScreen(viewModel)
        }

        composable<CreateAccountSetUsername> {
            CreateAccountSetUsernameScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<CreateAccountSetPassword> {
            CreateAccountSetPasswordScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<ChangePassword> {
            ChangePasswordScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<InvitePerson> {
            InvitePersonScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<CopyCode> {
            CopyInviteCodeScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<QrCode> {
            InviteQrScreen(
                viewModel = openEelViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

    }
}