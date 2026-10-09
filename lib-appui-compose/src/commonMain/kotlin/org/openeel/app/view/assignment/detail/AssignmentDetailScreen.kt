package world.respect.app.view.assignment.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import world.respect.app.components.RespectDataLoadHost
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.lib.xapi.ext.idStr
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentTaskProgress
import world.respect.lib.xapi.ext.addActivityToContextActivitiesGrouping
import world.respect.lib.xapi.ext.averageScore
import world.respect.lib.xapi.ext.extensionDeadlineAsInstantOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiAgent
import world.respect.shared.domain.xapi.createBlankAssignmentStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assigned_to
import world.respect.shared.generated.resources.average_str
import world.respect.shared.generated.resources.deadline
import world.respect.shared.generated.resources.no_matching_data_available_yet
import world.respect.shared.generated.resources.toggle_fullscreen
import world.respect.shared.util.AssignmentStatusFilter
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailUiState
import world.respect.shared.viewmodel.assignment.detail.AssignmentDetailViewModel

private const val NAME_COLUMN_WIDTH = 120
private const val TASK_COLUMN_WIDTH = 64
private const val HEADER_HEIGHT = 160

@Composable
fun AssignmentDetailScreen(
    viewModel: AssignmentDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentDetailScreen(
        uiState = uiState,
        onStatusFilterChanged = viewModel::onStatusFilterChanged,
        onClickTask = viewModel::onClickTask,
        onToggleFullscreen = viewModel::onToggleFullscreen,
        onClickScoreCell = viewModel::onClickScoreCell
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssignmentDetailScreen(
    uiState: AssignmentDetailUiState,
    onStatusFilterChanged: (AssignmentStatusFilter) -> Unit = { },
    onClickTask: (XapiActivity) -> Unit = { },
    onToggleFullscreen: () -> Unit = { },
    onClickScoreCell: (activityId: String, xapiActor: XapiActor) -> Unit = { _, _ -> },
) {
    val horizontalScrollState = rememberScrollState()

    val assignmentStmt = uiState.assignmentProgress.dataOrNull()?.assignmentStatement

    val stmtUuid = assignmentStmt?.id
    val deadlineInstant = remember(stmtUuid) {
        uiState.assignmentProgress.dataOrNull()?.assignmentStatement?.objectActivityOrNull()
            ?.definition?.extensionDeadlineAsInstantOrNull()
    }

    val deadlineDisplayStr = rememberFormattedDateTime(
        timeInMillis = deadlineInstant?.toEpochMilliseconds() ?: 0,
        timeZoneId = TimeZone.currentSystemDefault().id,
    )

    RespectDataLoadHost(uiState.assignmentProgress) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!uiState.isFullscreen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultItemPadding()
                    ) {
                        Text(
                            text = assignmentStmt?.objectActivityOrNull()?.definition?.description?.let {
                                langMapString(it)
                            } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.deadline),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = deadlineDisplayStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.assigned_to),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = uiState.assignmentProgress.dataOrNull()
                                        ?.assignmentStatement?.actor?.name.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultItemPadding(top = 16.dp, bottom = 4.dp),
                            thickness = 1.dp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = if (uiState.isFullscreen) 8.dp else 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssignmentStatusFilter.entries.forEach { filter ->
                        val count = uiState.statusCounts[filter] ?: 0
                        FilterChip(
                            selected = uiState.selectedStatusFilter == filter,
                            onClick = { onStatusFilterChanged(filter) },
                            label = {
                                Text(
                                    text = "${stringResource(filter.titleRes)} ($count)",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                if (uiState.isStudent) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        uiState.rowsToDisplay.firstOrNull()?.also { studentProgress ->
                            items(
                                items = studentProgress.progressPerTask
                            ) { taskProgress ->
                                uiState.tasks.firstOrNull {
                                    it.id == taskProgress.activityId
                                }?.also { activity ->
                                    AssignmentDetailTaskListItem(
                                        activity = activity,
                                        progress = taskProgress,
                                        taskInfoFlow = uiState.taskInfoFlow,
                                        onClickTask = onClickTask,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        val nameColWidth = minOf(maxWidth / 3, (NAME_COLUMN_WIDTH).dp)
                        val taskColWidth = (TASK_COLUMN_WIDTH).dp
                        val headerHeight = minOf(maxHeight / 2, (HEADER_HEIGHT).dp)

                        val assignmentResults = uiState.rowsToDisplay
                        val tasks = uiState.tasks

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // STICKY HEADER: Task Icons and Names
                            stickyHeader("header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(headerHeight)
                                        .background(MaterialTheme.colorScheme.surface),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(
                                        Modifier.width(nameColWidth).height(headerHeight)
                                    )

                                    // Scrollable Task Headers
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .horizontalScroll(horizontalScrollState),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        tasks.forEach { taskActivity ->
                                            AssignmentDetailTaskHeader(
                                                activity = taskActivity,
                                                taskInfoFlow = uiState.taskInfoFlow,
                                                taskColWidth = taskColWidth,
                                                headerHeight = headerHeight
                                            )
                                        }

                                        AssignmentDetailHeaderCell(
                                            title = stringResource(Res.string.average_str),
                                            width = taskColWidth,
                                            height = headerHeight
                                        )
                                    }
                                }
                            }

                            itemsIndexed(
                                items = assignmentResults,
                                key = { index, item -> item.actor.idStr ?: "a_$index" },
                            ) { _, studentAndProgress ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // Fixed Student Name Column
                                    StudentNameCell(
                                        name = studentAndProgress.actor.name ?: "",
                                        modifier = Modifier.width(nameColWidth)
                                            .height(taskColWidth)
                                            .padding(8.dp)
                                    )

                                    // Scrollable Grades/Progress Cells
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(horizontalScrollState)
                                    ) {
                                        studentAndProgress.progressPerTask.forEach { progressItem ->
                                            AssignmentDetailStudentProgressCell(
                                                progress = progressItem,
                                                modifier = Modifier.size(taskColWidth),
                                                onClickScoreCell = {
                                                    onClickScoreCell(
                                                        progressItem.activityId,
                                                        studentAndProgress.actor
                                                    )
                                                }
                                            )
                                        }

                                        AssignmentDetailStudentProgressCell(
                                            progress = studentAndProgress.progressPerTask.averageScore(),
                                            modifier = Modifier.size(taskColWidth)
                                        )
                                    }
                                }
                            }

                            if(assignmentResults.isEmpty()) {
                                item("no_students") {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                        )

                                        Text(stringResource(Res.string.no_matching_data_available_yet))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!uiState.isStudent) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .defaultItemPadding(bottom = 64.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("fullscreen_toggle_button"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(
                            imageVector = if (uiState.isFullscreen) {
                                Icons.Default.FullscreenExit
                            }else {
                                Icons.Default.Fullscreen
                            },
                            contentDescription = stringResource(Res.string.toggle_fullscreen)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}


internal const val mockAssignmentId = "http://example.com/assignments/1"

internal const val mockAssignmentTaskId1 = "http://example.app/math1"

internal val mockAssignmentTaskActivity = XapiActivity(
    id = mockAssignmentTaskId1,
    definition = XapiActivityDefinition(
        name = mapOf("en-US" to "Math1")
    )
)

internal val mockAssignmentStatement = createBlankAssignmentStatement(
    assignmentActivityId = mockAssignmentId,
    instructor = XapiAgent(
        name = "Alice Instructor",
        account = XapiAccount("http://example.com", "42")
    )
).addActivityToContextActivitiesGrouping(mockAssignmentTaskActivity)

internal val mockAssignmentUiState = AssignmentDetailUiState(
    assignmentProgress = DataReadyState(
        data = AssignmentAndProgress(
            assignmentStatement = mockAssignmentStatement,
            progress = listOf(
                XapiActorAndAssignmentProgress(
                    actor = XapiAgent(
                        name = "Lisa Simpson",
                        account = XapiAccount("http://example.com", "43")
                    ),
                    progressPerTask = listOf(
                        XapiAssignmentTaskProgress(
                            activityId = mockAssignmentTaskId1,
                            completed = true,
                            successful = true,
                            scoreScaled = 0.95f,
                        )
                    )
                ),
                XapiActorAndAssignmentProgress(
                    actor = XapiAgent(
                        name = "Bart Simpson",
                        account = XapiAccount("http://example.com", "44")
                    ),
                    progressPerTask = listOf(
                        XapiAssignmentTaskProgress(
                            activityId = mockAssignmentTaskId1,
                            completed = true,
                            successful = false,
                            scoreScaled = 0.2f,
                        )
                    )
                )
            )
        )
    )
)

@Composable
@Preview
fun AssignmentDetailScreenTeacherPreview() {
    AssignmentDetailScreen(
        uiState = mockAssignmentUiState
    )
}

@Composable
@Preview
fun AssignmentDetailScreenTeacherEmptyPreview() {
    AssignmentDetailScreen(
        uiState = mockAssignmentUiState.copy(
            assignmentProgress = DataReadyState(
                data = mockAssignmentUiState.assignmentProgress.dataOrNull()!!.copy(
                    progress = emptyList()
                )
            )
        )
    )
}
