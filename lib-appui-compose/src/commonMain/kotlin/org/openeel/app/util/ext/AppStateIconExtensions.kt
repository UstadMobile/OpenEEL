package world.respect.app.util.ext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.graphics.vector.ImageVector
import world.respect.shared.viewmodel.app.appstate.AppStateIcon

/**
 * Maps AppStateIcon enum values to actual ImageVector icons for Compose.
 */
fun AppStateIcon.toImageVector(): ImageVector = when (this) {
    AppStateIcon.MOVE -> Icons.Filled.DragHandle
    AppStateIcon.CLOSE -> Icons.Filled.Close
    AppStateIcon.DELETE -> Icons.Filled.Delete
    AppStateIcon.MORE_VERT -> Icons.Filled.MoreVert
}