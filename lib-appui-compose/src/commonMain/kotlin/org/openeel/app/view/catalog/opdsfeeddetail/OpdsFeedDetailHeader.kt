package world.respect.app.view.catalog.opdsfeeddetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.RespectQuickActionButton
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.ext.feedIconLinkOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign
import world.respect.shared.generated.resources.remix_collection
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.share
import world.respect.shared.viewmodel.catalog.opdsfeeddetail.OpdsFeedDetailUiState


@Composable
fun OpdsFeedDetailHeader(
    uiState: OpdsFeedDetailUiState,
    onClickShare: () -> Unit,
    onClickCopy: () -> Unit,
    onClickDelete: () -> Unit,
    onClickAssign: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val feedIconLink = uiState.feed.dataOrNull()?.feedIconLinkOrNull()
        val description = uiState.feed.dataOrNull()?.metadata?.description

        if(uiState.showFeedTitleInContent) {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = uiState.feed.dataOrNull()?.metadata?.title ?: "",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            feedIconLink?.also {
                RespectAsyncImage(
                    uri = it.href,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp),
                )
            }

            description?.also {
                Text(it)
            }
        }

        if(feedIconLink != null || description != null || uiState.showFeedTitleInContent) {
            HorizontalDivider()
        }

        if(uiState.quickActionsVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                RespectQuickActionButton(
                    modifier = Modifier.testTag("share_btn"),
                    labelText = stringResource(Res.string.share),
                    iconContent = {
                        Icon(Icons.Filled.Share, null)
                    },
                    onClick = onClickShare
                )

                RespectQuickActionButton(
                    modifier = Modifier.testTag("copy_btn"),
                    labelText = stringResource(Res.string.remix_collection),
                    iconContent = {
                        Icon(Icons.Filled.ContentCopy, null)
                    },
                    onClick = onClickCopy
                )

                if(uiState.showAssignButton) {
                    RespectQuickActionButton(
                        modifier = Modifier.testTag("header_assign_btn"),
                        labelText = stringResource(Res.string.assign),
                        iconContent = {
                            Icon(Icons.Filled.Task, null)
                        },
                        onClick = onClickAssign
                    )
                }

                if (uiState.isTeacherOrAdmin) {
                    RespectQuickActionButton(
                        modifier = Modifier.testTag("delete_btn"),
                        labelText = stringResource(Res.string.delete),
                        iconContent = {
                            Icon(Icons.Filled.Delete, null)
                        },
                        onClick = onClickDelete,
                    )
                }
            }

            HorizontalDivider()
        }
    }
}