package world.respect.shared.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.ascending
import world.respect.shared.generated.resources.descending


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