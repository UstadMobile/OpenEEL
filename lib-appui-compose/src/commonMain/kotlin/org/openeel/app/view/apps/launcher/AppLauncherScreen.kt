package world.respect.app.view.apps.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.langMapString
import world.respect.app.components.uiTextStringResource
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findIcons
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.empty
import world.respect.shared.generated.resources.empty_list
import world.respect.shared.generated.resources.more_info
import world.respect.shared.generated.resources.remove
import world.respect.shared.viewmodel.apps.launcher.AppLauncherUiState
import world.respect.shared.viewmodel.apps.launcher.AppLauncherViewModel

@Composable
fun AppLauncherScreen(
    viewModel: AppLauncherViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    AppLauncherScreen(
        uiState = uiState,
        onClickApp = { viewModel.onClickApp(it) },
        onClickRemove = { viewModel.onClickRemove(it) },
    )
}

@Composable
fun AppLauncherScreen(
    uiState: AppLauncherUiState,
    onClickApp: (DataLoadState<Publication>) -> Unit,
    onClickRemove: (DataLoadState<Publication>) -> Unit,
) {
    val apps = uiState.apps.dataOrNull() ?: emptyList()

    /**
     * Maestro end to end tests can be flakey: When running the login sometimes the keyboard stays
     * visible even after going from the login screen to the app launcher screen, which then
     * hides the bottom navigation buttons.
     */
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        keyboardController?.hide()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (apps.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Res.drawable.empty),
                    contentDescription = stringResource(resource = Res.string.empty_list),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                Text(
                    text = uiState.emptyListDescription?.let {
                        uiTextStringResource(it)
                    } ?: "",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    count = apps.size,
                    key = { index -> apps[index].objectActivityOrNull()?.id ?: index.toString() }
                ) { index ->
                    val statement = apps[index]
                    val respectAppFlow = remember(statement, uiState.respectPublicationForXapiStatement) {
                        uiState.respectPublicationForXapiStatement(statement)
                    }
                    val respectApp by respectAppFlow.collectAsState(NoDataLoadedState.notFound())

                    AppGridItem(
                        app = respectApp,
                        clickEnabled = uiState.isAppClickable(respectApp),
                        onClickApp = {
                            onClickApp(respectApp)
                        },
                        onClickRemove = {
                            onClickRemove(respectApp)
                        },
                        showRemove = uiState.canRemove,
                    )
                }
            }
        }
    }
}

@Composable
fun AppGridItem(
    app: DataLoadState<Publication>,
    clickEnabled: Boolean,
    onClickApp: () -> Unit,
    onClickRemove: () -> Unit,
    showRemove: Boolean = false,
) {
    val appData = app.dataOrNull()

    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(enabled = clickEnabled) {
                onClickApp()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            appData?.findIcons()?.firstOrNull()?.also { icon ->
                RespectAsyncImage(
                    uri = icon.href,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "",
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(resource = Res.string.more_info))
                        },
                        onClick = {
                            menuExpanded = false
                            onClickApp()
                        }
                    )
                    if(showRemove) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(resource = Res.string.remove))
                            },
                            onClick = {
                                menuExpanded = false
                                onClickRemove()
                            }
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = appData?.metadata?.title?.let { langMapString(it) } ?:"",
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "-")
            Text(text = "-")
        }
    }
}