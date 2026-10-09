package world.respect.app.app

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import kotlin.Boolean
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import world.respect.app.components.LocalAppLocale
import world.respect.app.components.customAppLocale
import world.respect.app.components.uiTextStringResource
import world.respect.app.effects.NavControllerLogEffect
import world.respect.datalayer.db.school.ext.isAdminOrTeacher
import world.respect.datalayer.db.school.ext.isParent
import world.respect.datalayer.school.ext.primaryRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.navigation.NavCommandEffect
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.biometric.BiometricAuthUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.assignments
import world.respect.shared.generated.resources.parents_only
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.continue_using_fingerprint_or
import world.respect.shared.generated.resources.people
import world.respect.shared.navigation.AccountList
import world.respect.shared.navigation.AssignmentList
import world.respect.shared.navigation.ClazzList
import world.respect.shared.navigation.Home
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.RespectComposeNavController
import world.respect.shared.resources.getUiTextString
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.SnackBarFlowDispatcher

/**
 * @property routeName this is required because it will be obfuscated in the release variant (the
 *           path as per currentBackStack.lastOrNull()?.destination?.route is preserved, but not
 *           the Route class name from AppRoutes)
 */
data class TopNavigationItem(
    val destRoute: Any,
    val icon: ImageVector,
    val label: StringResource,
    val routeName: String,
    val testTag: String,
)

private val routeNamePrefix = "world.respect.shared.navigation"

val APP_TOP_LEVEL_NAV_ITEMS = listOf(
    TopNavigationItem(
        destRoute = Home,
        icon = Icons.Filled.GridView,
        label = Res.string.home,
        routeName = "$routeNamePrefix.Home",
        testTag = "nav_home",
    ),
    TopNavigationItem(
        destRoute = AssignmentList,
        icon = Icons.Filled.ImportContacts,
        label = Res.string.assignments,
        routeName = "$routeNamePrefix.Assignment",
        testTag = "nav_assignments",
    ),
    TopNavigationItem(
        destRoute = ClazzList,
        icon = Icons.AutoMirrored.Filled.LibraryBooks,
        label = Res.string.classes,
        routeName = "$routeNamePrefix.ClazzList",
        testTag = "nav_classes",
    ),
    TopNavigationItem(
        destRoute = PersonList(isTopLevel = true),
        icon = Icons.Filled.Person,
        label = Res.string.people,
        routeName = "$routeNamePrefix.PersonList",
        testTag = "nav_people",
    ),
)
val APP_TOP_LEVEL_NAV_ITEMS_FOR_CHILD = listOf(
    TopNavigationItem(
        destRoute = AssignmentList,
        icon = Icons.Filled.ImportContacts,
        label = Res.string.assignments,
        routeName = "$routeNamePrefix.Assignment",
        testTag = "nav_assignments",
    ),
    TopNavigationItem(
        destRoute = Home,
        icon = Icons.Filled.GridView,
        label = Res.string.home,
        routeName = "$routeNamePrefix.Home",
        testTag = "nav_home",
    ),
)

/**
 * @param activityNavCommandFlow a flow that is received from the activity. When a link is opened
 *        and the app is already running, the Activity's onNewIntent will be invoked. If the app is
 *        started cold then InitDeepLinkUriProviderUseCase should be used.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App(
    activityNavCommandFlow: Flow<NavCommand>,
    widthClass: SizeClass = SizeClass.MEDIUM,
    useBottomBar: Boolean = true,
    onAppStateChanged: (AppUiState) -> Unit = { }
) {
    val appUiState = remember {
        mutableStateOf(
            AppUiState(
                navigationVisible = true,
                hideAppBar = false,
            )
        )
    }

    val navController = rememberNavController()
    val respectNavController = remember(Unit) {
        RespectComposeNavController(navController)
    }

    val coroutineScope = rememberCoroutineScope()

    val accountManager: RespectAccountManager = koinInject()
    val biometricAuthUseCase : BiometricAuthUseCase = koinInject()
    val activeAccount by accountManager.selectedAccountAndPersonFlow.collectAsState(null)
    val topLevelNavItems = if (activeAccount?.isChild == true) {
        APP_TOP_LEVEL_NAV_ITEMS_FOR_CHILD
    } else {
        APP_TOP_LEVEL_NAV_ITEMS
    }


    NavControllerLogEffect(navController)

    NavCommandEffect(
        navHostController = respectNavController,
        navCommandFlow = activityNavCommandFlow,
    )

    var appUiStateVal by appUiState
    LaunchedEffect(appUiStateVal) {
        onAppStateChanged(appUiStateVal)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val koin = getKoin()


    LaunchedEffect(Unit) {
        koin.get<SnackBarFlowDispatcher>().snackFlow.collectLatest { snack->
            val result = snackbarHostState.showSnackbar(
                message = getUiTextString(snack.message),
                actionLabel = snack.action?.let { getUiTextString(it) },
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                snack.onAction?.invoke()
            }
        }
    }

    CompositionLocalProvider(LocalWidthClass provides widthClass) {
        CompositionLocalProvider(LocalAppLocale provides customAppLocale) {
            Scaffold(
                topBar = {
                    if (!appUiStateVal.hideAppBar) {
                        RespectAppBar(
                            compactHeader = (widthClass != SizeClass.EXPANDED),
                            appUiState = appUiStateVal,
                            navController = navController,
                            topLevelItems = topLevelNavItems,
                            onProfileClick = {
                                if (activeAccount?.isChild == false || activeAccount?.person?.primaryRole() == PersonRoleEnum.STUDENT) {
                                    navController.navigate(AccountList)
                                } else {
                                    coroutineScope.launch {
                                        val result = biometricAuthUseCase(
                                            BiometricAuthUseCase.BiometricPromptData(
                                                title = getString(Res.string.parents_only),
                                                subtitle = getString(Res.string.continue_using_fingerprint_or),
                                                useDeviceCredential = true,
                                                negativeButtonText = getString(Res.string.cancel),
                                            )
                                        )

                                        if(result is BiometricAuthUseCase.BiometricResult.Success) {
                                            navController.navigate(AccountList)
                                        }
                                    }
                                }
                            },
                        )
                    }
                },
                bottomBar = {
                    var selectedTopLevelItemIndex by remember { mutableIntStateOf(0) }
                    if (useBottomBar) {
                        if (appUiStateVal.navigationVisible && !appUiStateVal.hideBottomNavigation) {
                            NavigationBar {
                                topLevelNavItems.forEachIndexed { index, item ->
                                    val skipIt = item.routeName == "$routeNamePrefix.Assignment" &&
                                            activeAccount?.person?.isParent() == true

                                    if(!skipIt) {
                                        NavigationBarItem(
                                            modifier = Modifier.testTag(item.testTag),
                                            icon = {
                                                Icon(item.icon, contentDescription = null)
                                            },
                                            label = {
                                                Text(stringResource(item.label), maxLines = 1)
                                            },
                                            selected = selectedTopLevelItemIndex == index,
                                            onClick = {
                                                navController.navigate(item.destRoute)  {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                                selectedTopLevelItemIndex = index
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (appUiStateVal.expandableFabState.visible) {
                        ExpandableFab(
                            state = appUiStateVal.expandableFabState,
                            onToggle = {
                                appUiStateVal = appUiStateVal.copy(
                                    expandableFabState = appUiStateVal.expandableFabState.copy(
                                        expanded = !appUiStateVal.expandableFabState.expanded
                                    )
                                )
                            },
                            onItemClick = { item ->
                                item.onClick()
                                appUiStateVal = appUiStateVal.copy(
                                    expandableFabState = appUiStateVal.expandableFabState.copy(
                                        expanded = false
                                    )
                                )
                            }
                        )
                    }
                    else if (appUiStateVal.fabState.visible) {
                        ExtendedFloatingActionButton(
                            modifier = Modifier.testTag("floating_action_button"),
                            onClick = appUiStateVal.fabState.onClick,
                            text = {
                                Text(
                                    modifier = Modifier.testTag("floating_action_button_text"),
                                    text = appUiStateVal.fabState.text?.let {
                                        uiTextStringResource(it)
                                    } ?: ""
                                )
                            },
                            icon = {
                                val imageVector = when (appUiStateVal.fabState.icon) {
                                    FabUiState.FabIcon.ADD -> Icons.Default.Add
                                    FabUiState.FabIcon.EDIT -> Icons.Default.Edit
                                    else -> null
                                }
                                if (imageVector != null) {
                                    Icon(
                                        imageVector = imageVector,
                                        contentDescription = null,
                                    )
                                }
                            }
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(snackbarHostState)
                },
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    respectNavController = respectNavController,
                    onSetAppUiState = {
                        appUiStateVal = it
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

}
