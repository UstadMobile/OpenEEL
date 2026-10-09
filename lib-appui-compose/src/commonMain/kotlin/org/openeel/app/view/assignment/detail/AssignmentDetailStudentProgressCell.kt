package world.respect.app.view.assignment.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.respect.shared.generated.resources.Res
import world.respect.lib.xapi.composites.XapiAssignmentTaskProgress
import world.respect.shared.generated.resources.completed_status
import world.respect.shared.generated.resources.failed
import kotlin.math.roundToInt

/**
 * Show a cell with:
 *  The percentage score, if available.
 *  A check icon if completed/successful, if no percentage score available.
 *  A cross icon if completed/unsuccessful, if not completed/successful
 *  A progress circle if progress set, but not completed
 *  Otherwise, a dash to indicate no info yet.
 */
@Composable
fun AssignmentDetailStudentProgressCell(
    progress: XapiAssignmentTaskProgress,
    modifier: Modifier = Modifier,
    onClickScoreCell: () -> Unit = {},
) {
    val score = progress.scoreScaled
    val progressPercentVal = progress.progress

    Box(
        modifier = modifier
        .clickable { onClickScoreCell() }
        .testTag("score_cell")) {
        when {
            score != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(
                            color = when (progress.successful) {
                                true -> Color.Green
                                false -> Color.Red
                                else -> Color.Transparent
                            }
                        )
                ) {
                    val currentTextStyle = LocalTextStyle.current
                    Text(
                        text = "${(score * 100).roundToInt()}%",
                        modifier = Modifier.align(Alignment.Center),
                        style = currentTextStyle.copy(
                            color = if(progress.successful != null) {
                                Color.White
                            }else {
                                currentTextStyle.color
                            }
                        )
                    )
                }
            }

            (progress.completed == true && progress.successful != false) -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    modifier = Modifier.align(Alignment.Center),
                    contentDescription = stringResource(Res.string.completed_status)
                )
            }

            (progress.completed == true && progress.successful == false) -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    modifier = Modifier.align(Alignment.Center),
                    contentDescription = stringResource(Res.string.failed)
                )
            }

            progressPercentVal != null -> {
                CircularProgressIndicator(
                    progress = { progressPercentVal.toFloat() / 100f },
                    strokeWidth = 4.dp,
                    modifier = Modifier.align(Alignment.Center)
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            else -> {
                Text(
                    text = "-",
                    modifier = Modifier.align(Alignment.Center),
                )
            }

        }
    }

}

@Composable
@Preview
fun AssignmentDetailStudentProgressCellPreview() {
    Row {
        AssignmentDetailStudentProgressCell(
            progress = XapiAssignmentTaskProgress(
                activityId = "",
                completed = true,
                successful = true,
                scoreScaled = 0.7f
            ),
            modifier = Modifier.size(48.dp),
        )

        AssignmentDetailStudentProgressCell(
            progress = XapiAssignmentTaskProgress(
                activityId = "",
                completed = true,
                successful = false,
                scoreScaled = 0.2f
            ),
            modifier = Modifier.size(48.dp),
        )

        AssignmentDetailStudentProgressCell(
            progress = XapiAssignmentTaskProgress(
                activityId = "",
                completed = true,
            ),
            modifier = Modifier.size(48.dp),
        )

        AssignmentDetailStudentProgressCell(
            progress = XapiAssignmentTaskProgress(
                activityId = "",
                completed = true,
                successful = false,
            ),
            modifier = Modifier.size(48.dp),
        )

        AssignmentDetailStudentProgressCell(
            progress = XapiAssignmentTaskProgress(
                activityId = "",
                progress = 75,
            ),
            modifier = Modifier.size(48.dp),
        )

        AssignmentDetailStudentProgressCell(
            progress = XapiAssignmentTaskProgress(
                activityId = "",
            ),
            modifier = Modifier.size(48.dp),
        )
    }

}
