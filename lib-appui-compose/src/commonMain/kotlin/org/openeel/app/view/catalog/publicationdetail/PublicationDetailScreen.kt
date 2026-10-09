package org.openeel.app.view.catalog.publicationdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libuicompose.theme.black
import com.ustadmobile.libuicompose.theme.white
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.app.RespectAsyncImage
import org.openeel.app.components.AlternativeLangLinks
import org.openeel.app.components.RespectDataLoadHost
import org.openeel.app.components.RespectOfflineItemStatusIcon
import org.openeel.app.components.RespectQuickActionButton
import org.openeel.app.components.defaultScreenPadding
import org.openeel.app.components.langMapString
import org.openeel.app.components.uiTextStringResource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.name
import org.openeel.shared.ext.alternateLanguageLinks
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.assign
import org.openeel.shared.generated.resources.bookmark
import org.openeel.shared.generated.resources.cancel
import org.openeel.shared.generated.resources.download
import org.openeel.shared.generated.resources.downloaded
import org.openeel.shared.generated.resources.license
import org.openeel.shared.generated.resources.open
import org.openeel.shared.generated.resources.subject
import org.openeel.shared.viewmodel.catalog.publicationdetail.PublicationDetailUiState
import org.openeel.shared.viewmodel.catalog.publicationdetail.PublicationDetailViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublicationDetailScreen(
    viewModel: PublicationDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    PublicationDetailScreen(
        uiState = uiState,
        onClickOpen = viewModel::onClickOpen,
        onClickDownload = viewModel::onClickDownload,
        onClickAssign = viewModel::onClickAssign,
        onClickApp = viewModel::onClickApp,
        onClickLicense = viewModel::onClickLicense,
        onClickBookmark = viewModel::onClickBookmark,
        onClickAlternativeLangVersion = viewModel::onClickAlternativeLangVersion,
    )
}

@Composable
fun PublicationDetailScreen(
    uiState: PublicationDetailUiState,
    onClickOpen: () -> Unit,
    onClickDownload: () -> Unit,
    onClickAssign: () -> Unit,
    onClickApp: (Publication) -> Unit,
    onClickBookmark: () -> Unit,
    onClickLicense: (Publication) -> Unit,
    onClickAlternativeLangVersion: (ReadiumLink) -> Unit,
) {
    RespectDataLoadHost(
        uiState.learningUnit,
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        val lessonDetail = uiState.learningUnit.dataOrNull()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val iconUrl = lessonDetail?.images?.firstOrNull()?.href

                iconUrl?.also { icon ->
                    RespectAsyncImage(
                        uri = icon,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = lessonDetail?.metadata?.title?.let { langMapString(it) } ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("publication_title"),
                    )

                    uiState.appDetail.dataOrNull()?.also { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onClickApp(app) }
                        ) {
                            val appIconUrl = app.images?.firstOrNull()?.href
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(white)
                                    .border(1.dp, black, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (appIconUrl != null) {
                                    RespectAsyncImage(
                                        uri = appIconUrl,
                                        contentDescription = "",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = langMapString(app.metadata.title),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    val duration = lessonDetail?.links?.firstOrNull { it.duration != null }?.duration?.seconds
                    if (duration != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "$duration",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onClickOpen,
                enabled = uiState.openButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.open))
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RespectQuickActionButton(
                    labelText = when (uiState.pinState.status) {
                        PublicationPinState.Status.IN_PROGRESS -> stringResource(Res.string.cancel)
                        PublicationPinState.Status.READY -> stringResource(Res.string.downloaded)
                        else -> stringResource(Res.string.download)
                    },
                    iconContent = {
                        RespectOfflineItemStatusIcon(
                            state = uiState.pinState,
                        )
                    },
                    onClick = onClickDownload,
                    enabled = uiState.openButtonEnabled,
                )

                RespectQuickActionButton(
                    imageVector = if (uiState.isBookmarked) {
                        Icons.Filled.Bookmark
                    } else {
                        Icons.Outlined.BookmarkBorder
                    },
                    labelText = stringResource(Res.string.bookmark),
                    onClick = onClickBookmark,
                    enabled = uiState.bookmarkButtonEnabled,
                )

                if (uiState.showAssignButton) {
                    RespectQuickActionButton(
                        imageVector = Icons.Filled.NearMe,
                        labelText = stringResource(Res.string.assign),
                        onClick = onClickAssign,
                        enabled = uiState.openButtonEnabled,
                    )
                }
            }

            HorizontalDivider()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.licenseLabel?.also { result ->
                    AssistChip(
                        onClick = { uiState.appDetail.dataOrNull()?.also { onClickLicense(it) } },
                        label = {
                            Text(
                                text = "${stringResource(Res.string.license)}: ${uiTextStringResource(result.title)}"
                            )
                        }
                    )
                }

                lessonDetail?.metadata?.subject?.forEach { subject ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "${stringResource(Res.string.subject)}: ${langMapString(subject.name)}"
                            )
                        }
                    )
                }
            }

            lessonDetail?.links?.alternateLanguageLinks()?.takeIf { it.isNotEmpty() }?.also { altLangLinks ->
                AlternativeLangLinks(
                    altLangLinks = altLangLinks,
                    onClickAlternativeLangVersion = onClickAlternativeLangVersion,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
    }
}
