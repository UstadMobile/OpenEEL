package org.openeel.app.view.catalog.opdsfeeddetail

import androidx.compose.runtime.Composable
import org.openeel.app.components.langMapString
import org.openeel.lib.opds.model.Publication


@Composable
fun PublicationListItem(
    publication: Publication,
    showCheckbox: Boolean = false,
    isSelected: Boolean = false,
    onClickPublication: (Publication) -> Unit,
    onLongPressPublication: ((Publication) -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    OpdsFeedListItem(
        title = langMapString(publication.metadata.title),
        iconUrl = publication.images?.firstOrNull()?.href,
        language = publication.metadata.language,
        duration = publication.metadata.duration,
        description = publication.metadata.description,
        showCheckbox = showCheckbox,
        isSelected = isSelected,
        onClick = { onClickPublication(publication) },
        onLongPress = if (onLongPressPublication != null) {
            { onLongPressPublication(publication) }
        }else {
            null
        },
        trailingContent = trailingContent,
    )
}