package org.openeel.shared.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.ascending
import org.openeel.shared.generated.resources.descending


/**
 * Description of sort order option - used in both UstadListSortHeader and UstadSortOptionsBottomSheet
 */
@Composable
fun SortOrderOption.description() : String  {
    return buildString {
        append(stringResource(fieldMessageId))
        order?.also { orderVal ->
            append(" (")
            if(orderVal) {
                append(stringResource(Res.string.ascending))
            }else {
                append(stringResource(Res.string.descending))
            }
            append(")")
        }
    }
}