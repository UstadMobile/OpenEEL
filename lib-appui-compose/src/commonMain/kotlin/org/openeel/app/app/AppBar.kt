package world.respect.app.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ustadmobile.libuicompose.theme.appBarSelectionModeBackgroundColor
import com.ustadmobile.libuicompose.theme.appBarSelectionModeContentColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.uiTextStringResource
import world.respect.app.util.ext.toImageVector
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.back
import world.respect.shared.generated.resources.search
import world.respect.shared.generated.resources.settings
import world.respect.shared.generated.resources.more_options
import world.respect.shared.util.ext.isLoading
import world.respect.shared.viewmodel.app.appstate.AppActionButton
import world.respect.shared.viewmodel.app.appstate.AppBarColors
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.app.appstate.LoadingUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectAppBar(
    compactHeader: Boolean,
    appUiState: AppUiState,
    navController: NavController,
    onProfileClick: () -> Unit = {},
    topLevelItems: List<TopNavigationItem>,
) {
    val currentBackStack by navController.currentBackStack.collectAsState()
    val currentRoute = currentBackStack.lastOrNull()?.destination?.route
    val isRootDest = remember(currentRoute) {
        topLevelItems.any { it.routeName == currentRoute }
    }

    val canGoBack = appUiState.showBackButton ?: !isRootDest && currentBackStack.size > 1

    val showUserAccountIcon = appUiState.userAccountIconVisible ?: !appUiState.actionBarButtonState.visible

    val accountManager: RespectAccountManager = koinInject()
    val activeAccount by accountManager.selectedAccountAndPersonFlow.collectAsState(null)
    val activeSessionVal = activeAccount?.session

    /*
     * Automated end-to-end test need to wait for pending writes to be saved to the server before
     * clearing the app state.
     */
    val koin = getKoin()
    val writeQueueSizeFlow: Flow<Int> = remember(activeSessionVal?.account?.scopeId) {
        if(activeSessionVal != null) {
            val scope = koin.getScope(activeSessionVal.account.scopeId)
            scope.get<RemoteWriteQueue>().queueSizeAsFlow().combine(
                scope.get<XapiRemoteWriteQueue>().queueSizeAsFlow()
            ) { queueSize, xapiQueueSize ->
                queueSize + xapiQueueSize
            }
        }else{
            flowOf(0)
        }
    }

    val pendingWriteCount by writeQueueSizeFlow.collectAsState(1)

    var searchActive by remember {
        mutableStateOf(false)
    }

    var searchHasFocus by remember {
        mutableStateOf(false)
    }

    val focusRequester = remember { FocusRequester() }

    //Focus the search box when it appears after the user clicks the search icon
    LaunchedEffect(searchActive) {
        if(compactHeader && searchActive)
            focusRequester.requestFocus()
    }

    var showOverflowMenu by remember { mutableStateOf(false) }

    val overflowActions = appUiState.actions.filter {
        it.display == AppActionButton.Companion.ActionButtonDisplay.OVERFLOW_MENU
    }
    val iconActions = appUiState.actions.filter {
        it.display == AppActionButton.Companion.ActionButtonDisplay.ICON
    }

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        TopAppBar(
            title = {
                Text(
                    text = appUiState.title?.let { uiTextStringResource(it) } ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("app_title"),
                )

                Text(
                    modifier = Modifier.testTag("pending_write_count")
                        .alpha(0f),
                    text = pendingWriteCount.toString(),
                    fontSize = 1.sp,
                )
            },
            navigationIcon = {
                if (canGoBack) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                }
            },
            actions = {
                if(appUiState.searchState.visible) {
                    if(!compactHeader || searchActive) {
                        OutlinedTextField(
                            modifier = Modifier.testTag("search_box")
                                .focusRequester(focusRequester)
                                .let {
                                    if(compactHeader || searchHasFocus) {
                                        it.width(320.dp)
                                    }else {
                                        it.width(192.dp)
                                    }
                                }
                                .onFocusChanged {
                                    searchHasFocus = it.hasFocus
                                },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if(searchActive) {
                                    IconButton(
                                        modifier = Modifier.testTag("close_search_button"),
                                        onClick = {
                                            appUiState.searchState.onSearchTextChanged("")
                                            searchActive = false
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "")
                                    }
                                }
                            },
                            value = appUiState.searchState.searchText,
                            placeholder = {
                                Text(text = stringResource(resource = Res.string.search))
                            },
                            onValueChange = appUiState.searchState.onSearchTextChanged,
                        )
                    }else {
                        IconButton(
                            modifier = Modifier.testTag("expand_search_icon_button"),
                            onClick = {
                                searchActive = true
                            }
                        ) {
                            Icon(Icons.Default.Search, contentDescription =
                                stringResource(Res.string.search)
                            )
                        }
                    }
                }

                if(appUiState.actionBarButtonState.visible) {
                    Button(
                        onClick = appUiState.actionBarButtonState.onClick,
                        enabled = appUiState.actionBarButtonState.enabled ?: !appUiState.isLoading,
                        modifier = Modifier.testTag("action_bar_button"),
                    ) {
                        Text(
                            text = appUiState.actionBarButtonState.text?.let {
                                uiTextStringResource(it)
                            } ?: ""
                        )
                    }
                }

                iconActions.forEach { action ->
                    IconButton(
                        onClick = action.onClick,
                        modifier = Modifier.testTag("action_${action.id}"),
                    ) {
                        Icon(
                            imageVector = action.icon.toImageVector(),
                            contentDescription = uiTextStringResource(action.contentDescription),
                        )
                    }
                }

                if (appUiState.settingsIconVisible == true) {
                    IconButton(
                        onClick = appUiState.onClickSettings ?: {},
                        modifier = Modifier.testTag("Settings")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.settings)
                        )
                    }
                }
                if(showUserAccountIcon) {
                    activeAccount?.also {
                        IconButton(
                            onClick = onProfileClick,
                            modifier = Modifier.testTag("user_account_icon"),
                        ) {
                            RespectPersonAvatar(name = it.person.fullName())
                        }
                    }
                }

                if (overflowActions.isNotEmpty()) {
                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.testTag("more_options"),
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(Res.string.more_options)
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            overflowActions.forEach { action ->
                                DropdownMenuItem(
                                    text = { Text(uiTextStringResource(action.text)) },
                                    onClick = {
                                        action.onClick()
                                        showOverflowMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            colors = if(appUiState.appBarColors == AppBarColors.STANDARD) {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            }else {
                val contentColor = MaterialTheme.colorScheme.appBarSelectionModeContentColor
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.appBarSelectionModeBackgroundColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor,
                )
            },
        )

        if(appUiState.loadingState.loadingState == LoadingUiState.State.INDETERMINATE) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
                    .height(2.dp)
                    .testTag("appbar_progress_bar")
            )
        }
    }


}