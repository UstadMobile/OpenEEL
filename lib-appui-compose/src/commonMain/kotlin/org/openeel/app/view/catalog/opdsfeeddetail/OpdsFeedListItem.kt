package world.respect.app.view.catalog.opdsfeeddetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.duration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedListItem(
    title: String,
    iconUrl: String?,
    description: String?,
    language: List<String>?,
    duration: Double?,
    showCheckbox: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
            ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                RespectAsyncImage(
                    uri = iconUrl,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(36.dp),
                )
            }
        },
        headlineContent = {
            Text(text = title)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                description?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    language?.let { Text(text = it.joinToString(", ")) }
                    duration?.let {
                        Text(text = "${stringResource(Res.string.duration)} - $it")
                    }
                }
            }
        },
        trailingContent = if (showCheckbox) {
            {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    modifier = Modifier.testTag("check_box"),
                )
            }
        } else {
            null
        },
    )
}