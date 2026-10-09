package world.respect.app.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StringBuilder.appendStringRes(res: StringResource) {
    append(stringResource(res))
}
