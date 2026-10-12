package org.openeel.app.view.assignment.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.openeel.app.components.langMapString
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.LangMap
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.findIcons
import org.openeel.lib.xapi.ext.webPubManifestAsUrlOrNull
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.libutil.ext.resolve

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
