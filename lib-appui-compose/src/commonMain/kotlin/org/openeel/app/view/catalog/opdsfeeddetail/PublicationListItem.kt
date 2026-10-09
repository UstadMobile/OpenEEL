package world.respect.app.view.catalog.opdsfeeddetail

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.langMapString
import world.respect.lib.opds.model.Publication
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.duration


@Composable
fun PublicationListItem(
    publication: Publication,
    onClickPublication: (Publication) -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable {
                onClickPublication(publication)
            },

        trailingContent = trailingContent,

        leadingContent = {
            val iconUrl = publication.images?.firstOrNull()?.href

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                iconUrl.also { icon ->
                    RespectAsyncImage(
                        uri = icon,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
            }
        },

        headlineContent = {
            Text(
                text = langMapString(publication.metadata.title)
            )
        },

        supportingContent = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.classes),
                )
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    publication.metadata.language
                        ?.let { language ->
                            Text(
                                text = language.joinToString(", ")
                            )
                        }

                    publication.metadata.duration
                        ?.let { duration ->
                            Text(text = "${stringResource(Res.string.duration)} - $duration")
                        }
                }
            }
        },
    )
}