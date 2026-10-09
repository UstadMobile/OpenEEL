package world.respect.app.view.assignment.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import world.respect.app.components.langMapString
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findIcons
import world.respect.lib.xapi.ext.webPubManifestAsUrlOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.libutil.ext.resolve

@Composable
fun AssignmentDetailTaskHeader(
    activity: XapiActivity,
    taskInfoFlow: (Url) -> Flow<DataLoadState<Publication>>,
    taskColWidth: Dp,
    headerHeight: Dp,
) {
    val manifestUrl = activity.definition?.webPubManifestAsUrlOrNull()

    val infoFlow = remember(manifestUrl) {
        manifestUrl?.let { taskInfoFlow(it) } ?: flowOf(DataLoadingState())
    }
    val info by infoFlow.collectAsState(DataLoadingState())

    val title = info.dataOrNull()?.metadata?.title
        ?: activity.definition?.name?.let { LangMap.fromMap(it) }

    val iconUrl = info.dataOrNull()?.findIcons()?.firstOrNull()?.let {
        manifestUrl?.resolve(it.href)
    }

    AssignmentDetailHeaderCell(
        title = title?.let { langMapString(it) } ?: "",
        iconUrl = iconUrl,
        width = taskColWidth,
        height = headerHeight
    )
}
