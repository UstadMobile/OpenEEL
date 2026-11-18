package org.openeel.app.view.catalog.opdsfeeddetail

import androidx.compose.runtime.Composable
import org.openeel.datalayer.school.opds.ext.findIcons
import org.openeel.lib.opds.model.ReadiumLink


@Composable
fun NavigationListItem(
    navigation: ReadiumLink,
    showCheckbox: Boolean = false,
    isSelected: Boolean = false,
    onClickNavigation: (ReadiumLink) -> Unit,
    onLongPress: () -> Unit,
    description: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    OpdsFeedListItem(
        title = navigation.title
            ?.takeIf { it != "null" && it.isNotBlank() }
            ?: navigation.href,
        iconUrl = navigation.findIcons().firstOrNull()?.href,
        description = description,
        language = navigation.language,
        duration = navigation.duration,
        showCheckbox = showCheckbox,
        isSelected = isSelected,
        onClick = { onClickNavigation(navigation) },
        onLongPress = onLongPress,
        trailingContent = trailingContent,
    )
}