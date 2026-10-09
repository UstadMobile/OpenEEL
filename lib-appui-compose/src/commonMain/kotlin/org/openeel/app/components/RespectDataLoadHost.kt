package world.respect.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.not_found
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText

/**
 * Simple component that will normally pass simply pass through the contents. If the data load
 * state is an error, it will NOT display the contents and will instead show the an error message
 *
 * It essentially functions similarly to a guard route.
 *
 * @param dataLoadState The data load state as above.
 */
@Composable
fun RespectDataLoadHost(
    dataLoadState: DataLoadState<*>,
    modifier: Modifier = Modifier,
    contents: @Composable () -> Unit
) {
    val exception = (dataLoadState as? DataErrorResult<*>)?.error

    if(exception != null || dataLoadState is NoDataLoadedState<*>) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
            )

            Text(
                text = uiTextStringResource(
                    exception?.getUiTextOrGeneric() ?: Res.string.not_found.asUiText()
                )
            )
        }
    }else {
        contents()
    }
}