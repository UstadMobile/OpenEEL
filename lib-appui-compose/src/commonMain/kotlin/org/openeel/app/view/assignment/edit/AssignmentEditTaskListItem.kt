package org.openeel.app.view.assignment.edit

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
import org.openeel.app.components.langMapString
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.LangMap
import org.openeel.lib.opds.model.asLangMap
import org.openeel.lib.opds.model.findIcons
import org.openeel.lib.xapi.ext.webPubManifestAsUrlOrNull
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.libutil.ext.resolve
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.close
import org.openeel.shared.generated.resources.task_image
import org.openeel.shared.viewmodel.assignment.edit.AssignmentEditUiState


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