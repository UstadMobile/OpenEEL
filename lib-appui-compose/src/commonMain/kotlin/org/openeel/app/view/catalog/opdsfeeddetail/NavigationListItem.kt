package org.openeel.app.view.catalog.opdsfeeddetail

import androidx.compose.runtime.Composable
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.shared.viewmodel.catalog.opdsfeeddetail.OpdsFeedDetailViewModel.Companion.ICON


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
        iconUrl = navigation.alternate?.find {
            it.rel?.contains(ICON) == true
        }?.href,
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