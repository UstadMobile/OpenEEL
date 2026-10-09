package world.respect.app.view.assignment.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.task_image

@Composable
fun AssignmentDetailHeaderCell(
    title: String,
    iconUrl: Url? = null,
    width: Dp,
    height: Dp
) {
    Column(
        modifier = Modifier
            .width(width)
            .height(height),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.rotate(-90f),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.labelSmall
            )
        }
        if (iconUrl != null) {
            AsyncImage(
                model = iconUrl.toString(),
                contentDescription = stringResource(Res.string.task_image),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
@Preview
fun AssignmentDetailHeaderCellPreview() {
    AssignmentDetailHeaderCell(
        title = "Hello World Task",
        width = 80.dp,
        height = 200.dp,
    )
}

