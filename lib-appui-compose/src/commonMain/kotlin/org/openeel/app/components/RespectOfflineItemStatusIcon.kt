package world.respect.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.libcache.PublicationPinState

@Composable
fun RespectOfflineItemStatusIcon(
    state: PublicationPinState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {

        Icon(
            imageVector = when(state.status) {
                PublicationPinState.Status.READY -> Icons.Default.OfflinePin
                PublicationPinState.Status.IN_PROGRESS -> Icons.Default.Stop
                else -> Icons.Default.FileDownload
            },
            contentDescription = null,
        )

        when (state.status) {
            PublicationPinState.Status.PREPARING -> {
                CircularProgressIndicator()
            }

            PublicationPinState.Status.IN_PROGRESS -> {
                CircularProgressIndicator(
                    progress = {
                        state.transferred.toFloat() / state.totalSize.toFloat().let {
                            if(it == 0f) 1f else it
                        }
                    }
                )
            }

            else -> {
                //no progress indicator
            }
        }
    }
}