package world.respect.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import world.respect.app.view.acknowledgement.AcknowledgementScreen
import world.respect.app.view.apps.detail.AppsDetailScreen
import world.respect.app.view.apps.enterlink.EnterLinkScreen
import world.respect.app.view.apps.list.AppListScreen
import world.respect.app.view.assignment.detail.AssignmentDetailScreen
import world.respect.app.view.assignment.edit.AssignmentEditScreen
import world.respect.app.view.assignment.list.AssignmentListScreen
import world.respect.app.view.catalog.bookmark.BookmarkListScreen
import world.respect.app.view.clazz.detail.ClazzDetailScreen
import world.respect.app.view.clazz.edit.ClazzEditScreen
import world.respect.app.view.clazz.list.ClazzListScreen
import world.respect.app.view.enrollment.edit.EnrollmentEditScreen
import world.respect.app.view.enrollment.list.EnrollmentListScreen
import world.respect.app.view.home.HomeScreen
import world.respect.app.view.catalog.publicationdetail.PublicationDetailScreen
import world.respect.app.view.catalog.opdsfeeddetail.OpdsFeedDetailScreen
import world.respect.app.view.manageuser.accountlist.AccountListScreen
import world.respect.app.view.manageuser.acceptinvite.AcceptInviteScreen
import world.respect.app.view.manageuser.createaccount.CreateAccountScreen
import world.respect.app.view.manageuser.enterpasswordsignup.EnterPasswordSignupScreen
import world.respect.app.view.manageuser.getstarted.GetStartedScreen
import world.respect.app.view.manageuser.howpasskeywork.HowPasskeyWorksScreen
import world.respect.app.view.manageuser.enterinvitecode.EnterInviteCodeScreen
import world.respect.app.view.manageuser.login.LoginScreen
import world.respect.app.view.manageuser.otheroption.OtherOptionsScreen
import world.respect.app.view.manageuser.otheroptionsignup.OtherOptionsSignupScreen
import world.respect.app.view.manageuser.sharefeedback.ShareFeedbackScreen
import world.respect.app.view.manageuser.signup.SignupScreen
import world.respect.app.view.manageuser.termsandcondition.TermsAndConditionScreen
import world.respect.app.view.manageuser.waitingforapproval.WaitingForApprovalScreen
import world.respect.app.view.onboarding.OnboardingScreen
import world.respect.app.view.person.changepassword.ChangePasswordScreen
import world.respect.app.view.person.copycode.CopyInviteCodeScreen
import world.respect.app.view.person.detail.PersonDetailScreen
import world.respect.app.view.person.edit.PersonEditScreen
import world.respect.app.view.person.inviteperson.InvitePersonScreen
import world.respect.app.view.person.list.PersonListScreen
import world.respect.app.view.person.manageaccount.ManageAccountScreen
import world.respect.app.view.person.passkeyList.PasskeyListScreen
import world.respect.app.view.person.qrcode.InviteQrScreen
import world.respect.app.view.person.setusernameandpassword.CreateAccountSetPasswordScreen
import world.respect.app.view.person.setusernameandpassword.CreateAccountSetUsernameScreen
import world.respect.app.view.catalog.opdsfeededitaddlink.OpdsFeedEditAddLinkScreen
import world.respect.app.view.catalog.opdsfeededit.OpdsFeedEditScreen
import world.respect.app.view.catalog.opdsfeedlist.OpdsFeedListScreen
import world.respect.app.view.catalog.opdsfeedshare.PlaylistShareScreen
import world.respect.app.view.report.detail.ReportDetailScreen
import world.respect.app.view.report.edit.ReportEditScreen
import world.respect.app.view.report.filteredit.ReportFilterEditScreen
import world.respect.app.view.report.indicator.detail.IndicatorDetailScreen
import world.respect.app.view.report.indicator.edit.IndictorEditScreen
import world.respect.app.view.report.indicator.list.IndicatorListScreen
import world.respect.app.view.report.list.ReportListScreen
import world.respect.app.view.report.list.ReportTemplateListScreen
import world.respect.app.view.scanqrcode.ScanQRCodeScreen
import world.respect.app.view.schooldirectory.edit.SchoolDirectoryEditScreen
import world.respect.app.view.schooldirectory.list.SchoolDirectoryListScreen
import world.respect.app.view.settings.SettingsScreen
import world.respect.app.view.sharedschooldevice.SchoolSettingsScreen
import world.respect.app.view.sharedschooldevice.SharedDevicesSettingsScreen
import world.respect.app.view.sharedschooldevice.TeacherPinConfirmationScreen
import world.respect.app.view.sharedschooldevice.login.SelectClassScreen
import world.respect.app.view.sharedschooldevice.login.StudentListScreen
import world.respect.app.view.statement.detail.RawStatementScreen
import world.respect.app.view.statement.detail.StatementDetailScreen
import world.respect.app.view.statement.list.StatementListScreen
import world.respect.app.viewmodel.respectViewModel
import world.respect.shared.navigation.AccountList
import world.respect.shared.navigation.Acknowledgement
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.AssignmentDetail
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.ChangePassword
import world.respect.shared.navigation.ClazzDetail
import world.respect.shared.navigation.ClazzEdit
import world.respect.shared.navigation.ClazzList
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.BookmarkList
import world.respect.shared.navigation.CopyCode
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.CreateAccountSetPassword
import world.respect.shared.navigation.CreateAccountSetUsername
import world.respect.shared.navigation.EnrollmentEdit
import world.respect.shared.navigation.EnrollmentList
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.IndicatorDetail
import world.respect.shared.navigation.IndicatorList
import world.respect.shared.navigation.IndictorEdit
import world.respect.shared.navigation.InvitePerson
import world.respect.shared.navigation.EnterInviteCode
import world.respect.shared.navigation.ExternalLinkEdit
import world.respect.shared.navigation.Home
import world.respect.shared.navigation.PublicationDetail
import world.respect.shared.navigation.OpdsFeedDetail
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.Onboarding
import world.respect.shared.navigation.OtherOption
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.navigation.PasskeyList
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.OpdsFeedEdit
import world.respect.shared.navigation.PlaylistList
import world.respect.shared.navigation.PlaylistShare
import world.respect.shared.navigation.QrCode
import world.respect.shared.navigation.RawStatement
import world.respect.shared.navigation.Report
import world.respect.shared.navigation.ReportDetail
import world.respect.shared.navigation.ReportEdit
import world.respect.shared.navigation.ReportEditFilter
import world.respect.shared.navigation.ReportTemplateList
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.navigation.RespectComposeNavController
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.SchoolDirectoryEdit
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.navigation.SchoolSettings
import world.respect.shared.navigation.SelectClass
import world.respect.shared.navigation.Settings
import world.respect.shared.navigation.ShareFeedback
import world.respect.shared.navigation.SharedDevicesSettings
import world.respect.shared.navigation.SignupScreen
import world.respect.shared.navigation.StatementDetail
import world.respect.shared.navigation.StatementList
import world.respect.shared.navigation.StudentList
import world.respect.shared.navigation.TeacherPinConfirmation
import world.respect.shared.navigation.TermsAndCondition
import world.respect.shared.navigation.WaitingForApproval
import world.respect.shared.viewmodel.acknowledgement.AcknowledgementViewModel
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.apps.detail.AppsDetailViewModel
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel
import world.respect.shared.viewmodel.apps.list.AppListViewModel
import world.respect.shared.viewmodel.clazz.detail.ClazzDetailViewModel
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel
import world.respect.shared.viewmodel.clazz.list.ClazzListViewModel
import world.respect.shared.viewmodel.enrollment.edit.EnrollmentEditViewModel
import world.respect.shared.viewmodel.enrollment.list.EnrollmentListViewModel
import world.respect.shared.viewmodel.catalog.publicationdetail.PublicationDetailViewModel
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
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel
import world.respect.shared.viewmodel.report.edit.ReportEditViewModel
import world.respect.shared.viewmodel.report.filteredit.ReportFilterEditViewModel
import world.respect.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel
import world.respect.shared.viewmodel.report.indictor.edit.IndicatorEditViewModel
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListViewModel
import world.respect.shared.viewmodel.report.list.ReportListViewModel
import world.respect.shared.viewmodel.report.list.ReportTemplateListViewModel
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditViewModel
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListViewModel
import world.respect.shared.viewmodel.settings.SettingsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    respectNavController: RespectComposeNavController = remember(Unit) {
        RespectComposeNavController(navController)
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
            val viewModel: AcknowledgementViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AcknowledgementScreen(viewModel)
        }

        composable<Onboarding> {
            val viewModel: OnboardingViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OnboardingScreen(viewModel)
        }

        composable<LoginScreen> {
            val viewModel: LoginViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            LoginScreen(viewModel)
        }

        composable<EnterInviteCode> {
            val viewModel: EnterInviteCodeViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterInviteCodeScreen(viewModel)
        }

        composable<RespectAppLauncher> {
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
            val viewModel: AppsDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AppsDetailScreen(viewModel = viewModel)
        }

        composable<AssignmentList> {
            AssignmentListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<AssignmentEdit> {
            AssignmentEditScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<AssignmentDetail> {
            AssignmentDetailScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<StatementList>{
            StatementListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<StatementDetail> {
            StatementDetailScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<RawStatement> {
            RawStatementScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<BookmarkList> {
            BookmarkListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<ClazzList> {
            val viewModel: ClazzListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzListScreen(viewModel = viewModel)
        }

        composable<ClazzEdit> {
            val viewModel: ClazzEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzEditScreen(viewModel = viewModel)
        }

        composable<ClazzDetail> {
            val viewModel: ClazzDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ClazzDetailScreen(viewModel = viewModel)
        }

        composable<EnrollmentList> {
            val viewModel: EnrollmentListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnrollmentListScreen(viewModel = viewModel)
        }

        composable<EnrollmentEdit> {
            val viewModel: EnrollmentEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnrollmentEditScreen(viewModel = viewModel)
        }

        composable<ReportDetail> {
            val viewModel: ReportDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportDetailScreen(navController = navController, viewModel = viewModel)
        }

        composable<ReportEdit> {
            val viewModel: ReportEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportEditScreen(viewModel = viewModel)
        }

        composable<Report> {
            val viewModel: ReportListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportListScreen(viewModel = viewModel)
        }

        composable<ReportTemplateList> {
            val viewModel: ReportTemplateListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportTemplateListScreen(viewModel = viewModel)
        }

        composable<IndictorEdit> {
            val viewModel: IndicatorEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndictorEditScreen(viewModel = viewModel)
        }

        composable<ReportEditFilter> {
            val viewModel: ReportFilterEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            ReportFilterEditScreen(navController = navController, viewModel = viewModel)
        }

        composable<IndicatorList> {
            val viewModel: IndicatorListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndicatorListScreen(viewModel = viewModel)
        }

        composable<IndicatorDetail> {
            val viewModel: IndicatorDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            IndicatorDetailScreen(navController = navController, viewModel = viewModel)
        }

        composable<HowPasskeyWorks> {
            val viewModel: HowPasskeyWorksViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            HowPasskeyWorksScreen(viewModel = viewModel)
        }

        composable<RespectAppList> {
            val viewModel: AppListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AppListScreen(viewModel = viewModel)
        }

        composable<EnterLink> {
            val viewModel: EnterLinkViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterLinkScreen(viewModel = viewModel)
        }

        composable<GetStartedScreen> {
            val viewModel: GetStartedViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            GetStartedScreen(viewModel = viewModel)
        }

        composable<OtherOption> {
            val viewModel: OtherOptionsViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OtherOptionsScreen(viewModel = viewModel)
        }

        composable<OpdsFeedDetail> {
            OpdsFeedDetailScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<OtherOptionsSignup> {
            val viewModel: OtherOptionsSignupViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            OtherOptionsSignupScreen(viewModel = viewModel)
        }

        composable<EnterPasswordSignup> {
            val viewModel: EnterPasswordSignupViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            EnterPasswordSignupScreen(viewModel = viewModel)
        }

        composable<PublicationDetail> {
            val viewModel: PublicationDetailViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            PublicationDetailScreen(viewModel = viewModel)
        }

        composable<SignupScreen> {
            val viewModel: SignupViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SignupScreen(viewModel = viewModel)
        }

        composable<AcceptInvite> {
            val viewModel: AcceptInviteViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            AcceptInviteScreen(viewModel = viewModel)
        }

        composable<TermsAndCondition> {
            val viewModel: TermsAndConditionViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            TermsAndConditionScreen(viewModel = viewModel)
        }

        composable<CreateAccount> {
            val viewModel: CreateAccountViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            CreateAccountScreen(viewModel = viewModel)
        }

        composable<WaitingForApproval> {
            val viewModel: WaitingForApprovalViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            WaitingForApprovalScreen(viewModel = viewModel)
        }

        composable<AccountList> {
            AccountListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<ShareFeedback> {
            ShareFeedbackScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonList> {
            PersonListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonDetail> {
            PersonDetailScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<ManageAccount> {
            ManageAccountScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PasskeyList> {
            PasskeyListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PersonEdit> {
            PersonEditScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<Settings> {
            val viewModel: SettingsViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SettingsScreen(viewModel = viewModel)
        }

        composable<ScanQRCode> {
            ScanQRCodeScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController
                )
            )
        }

        composable<PlaylistList> {
            OpdsFeedListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<OpdsFeedEdit> {
            OpdsFeedEditScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<PlaylistShare> {
            PlaylistShareScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<ExternalLinkEdit> {
            OpdsFeedEditAddLinkScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<SchoolSettings> {
            SchoolSettingsScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<SharedDevicesSettings> {
            SharedDevicesSettingsScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<SelectClass> {
            SelectClassScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
        composable<TeacherPinConfirmation> {
            TeacherPinConfirmationScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }


        composable<SchoolDirectoryList> {
            val viewModel: SchoolDirectoryListViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SchoolDirectoryListScreen(viewModel)
        }

        composable<SchoolDirectoryEdit> {
            val viewModel: SchoolDirectoryEditViewModel = respectViewModel(
                onSetAppUiState = onSetAppUiState,
                navController = respectNavController
            )
            SchoolDirectoryEditScreen(viewModel)
        }

        composable<CreateAccountSetUsername> {
            CreateAccountSetUsernameScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<CreateAccountSetPassword> {
            CreateAccountSetPasswordScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<ChangePassword> {
            ChangePasswordScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<InvitePerson> {
            InvitePersonScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<CopyCode> {
            CopyInviteCodeScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }

        composable<QrCode> {
            InviteQrScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }


        composable<StudentList> {
            StudentListScreen(
                viewModel = respectViewModel(
                    onSetAppUiState = onSetAppUiState,
                    navController = respectNavController,
                )
            )
        }
    }
}