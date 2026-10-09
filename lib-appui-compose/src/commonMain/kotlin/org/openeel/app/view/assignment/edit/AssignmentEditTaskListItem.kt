package world.respect.app.view.assignment.edit

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.langMapString
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.asLangMap
import world.respect.lib.opds.model.findIcons
import world.respect.lib.xapi.ext.webPubManifestAsUrlOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.libutil.ext.resolve
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.close
import world.respect.shared.generated.resources.task_image
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditUiState


@Composable
fun AssignmentEditTaskListItem(
    taskActivity: XapiActivity,
    uiState: AssignmentEditUiState,
    onRemove: () -> Unit
) {
    val manifestUrl = taskActivity.definition?.webPubManifestAsUrlOrNull()

    val infoFlow = remember(manifestUrl) {
        if(manifestUrl != null)
            uiState.learningUnitInfoFlow(manifestUrl)
        else
            flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
    }

    val info by infoFlow.collectAsState(DataLoadingState())

    val data = info.dataOrNull()

    ListItem(
        headlineContent = {
            Text(
                text = langMapString(
                    info.dataOrNull()?.metadata?.title ?: LangMap.EMPTY
                )
            )
        },
        supportingContent = {
            Text(
                text = data?.metadata?.description ?: "",
            )
        },
        leadingContent = {
            val iconLink = data?.findIcons()?.firstOrNull()
            AsyncImage(
                model = iconLink?.let {
                    manifestUrl?.resolve(it.href)?.toString()
                },
                contentDescription = stringResource(Res.string.task_image),
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp))
            )
        },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(Res.string.close),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}