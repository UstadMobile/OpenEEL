package world.respect.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.nothing_here_at_the_moment

@Composable
fun RespectEmptyListComponent(
    modifier: Modifier = Modifier,
    text: String = stringResource(Res.string.nothing_here_at_the_moment)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CropFree,
            contentDescription = null,
        )

        Text(text = text)
    }
}


@Preview
@Composable
fun RespectEmptyListComponentPreview() {
    RespectEmptyListComponent()
}