package world.respect.app.view.assignment.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.LangMapTextField
import world.respect.app.components.RespectLocalDateTimeField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.school.model.Clazz
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.copyWithObjectActivityDescription
import world.respect.lib.xapi.ext.copyWithObjectActivityName
import world.respect.lib.xapi.ext.extensionDeadlineAsInstantOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.domain.xapi.withDeadline
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign_to
import world.respect.shared.generated.resources.assignment_title
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.task
import world.respect.shared.generated.resources.assignment_name
import world.respect.shared.generated.resources.lesson_assessment
import world.respect.shared.generated.resources.due_date
import world.respect.shared.generated.resources.fingerprint
import world.respect.shared.generated.resources.no_tasks_selected_yet
import world.respect.shared.generated.resources.please_click_plus_button_to_add_one
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.task
import world.respect.shared.generated.resources.tasks
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditUiState
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun AssignmentEditScreen(
    viewModel: AssignmentEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    AssignmentEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
        onAssigneeTextChanged = viewModel::onAssigneeTextChanged,
        onClickAddLearningUnit = viewModel::onClickAddLearningUnit,
        onAssigneeClassSelected = viewModel::onAssigneeClassSelected,
        onClickRemoveLearningUnit = viewModel::onClickRemoveLearningUnit,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun AssignmentEditScreen(
    uiState: AssignmentEditUiState,
    onEntityChanged: (XapiStatement) -> Unit,
    onAssigneeTextChanged: (String) -> Unit,
    onAssigneeClassSelected: (Clazz) -> Unit,
    onClickAddLearningUnit: () -> Unit,
    onClickRemoveLearningUnit: (XapiActivity) -> Unit,
) {
    val assignment = uiState.statementData.dataOrNull()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        LangMapTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("title"),
            value = assignment?.objectActivityOrNull()?.definition?.name ?: emptyMap(),
            onValueChange = { value ->
                assignment?.copyWithObjectActivityName(value)?.also { onEntityChanged(it) }
            },
            label = {
                Text(stringResource(Res.string.assignment_title) + "*")
            },
            supportingText = {
                Text(uiTextStringResource(uiState.nameError ?: Res.string.required.asUiText()))
            },
            enabled = uiState.fieldsEnabled,
        )

        var expanded by remember { mutableStateOf(false) }

        //As per https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ExposedDropdownMenuBox(kotlin.Boolean,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Function1)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("class_dropdown_textfield")
                    .menuAnchor(MenuAnchorType.PrimaryEditable),
                value = uiState.assignee,
                label = {
                    Text(stringResource(Res.string.assign_to))
                },
                onValueChange = {
                    onAssigneeTextChanged(it)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                        modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable),
                    )
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                supportingText = {
                    Text(uiTextStringResource(uiState.classError ?: Res.string.required.asUiText()))
                },
                isError = uiState.classError != null,
            )

            ExposedDropdownMenu(
                modifier = Modifier.heightIn(max = 280.dp),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                uiState.classOptions.forEach { clazz ->
                    DropdownMenuItem(
                        text = {
                            Text(clazz.title)
                        },
                        onClick = {
                            expanded = false
                            onAssigneeClassSelected(clazz)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        LangMapTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("description"),
            value = assignment?.objectActivityOrNull()?.definition?.description ?: emptyMap(),
            label = {
                Text(stringResource(Res.string.description))
            },
            onValueChange = { value ->
                assignment?.copyWithObjectActivityDescription(value)?.also { onEntityChanged(it) }
            },
            enabled = uiState.fieldsEnabled,
        )

        RespectLocalDateTimeField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            value = assignment?.objectActivityOrNull()?.definition?.extensionDeadlineAsInstantOrNull()
                ?.toLocalDateTime(TimeZone.currentSystemDefault()),
            onValueChanged = { newDeadline ->
                assignment?.also {
                    onEntityChanged(
                        it.withDeadline(newDeadline?.toInstant(TimeZone.currentSystemDefault()))
                    )
                }
            },
            dateLabel = {
                Text(stringResource(Res.string.due_date))
            },
            dateTestTag = "due_date",
            timeTestTag = "due_time",
        )

        Text(
            modifier = Modifier.defaultItemPadding(),
            text = stringResource(Res.string.tasks),
            style = MaterialTheme.typography.titleMedium
        )

        ListItem(
            modifier = Modifier.clickable {
                onClickAddLearningUnit()
            },
            headlineContent = {
                Text(stringResource(Res.string.task))
            },
            leadingContent = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                )
            }
        )

        val currentTasks = assignment?.context?.contextActivities?.grouping ?: emptyList()

        if (currentTasks.isEmpty()) {
            EmptyTasksIllustration()
        } else {
            currentTasks.forEach { learningUnit ->
                AssignmentEditTaskListItem(
                    taskActivity = learningUnit,
                    uiState = uiState,
                    onRemove = { onClickRemoveLearningUnit(learningUnit) }
                )
            }
        }
    }
}


@Composable
fun EmptyTasksIllustration() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(Res.drawable.fingerprint),
            contentDescription = stringResource(Res.string.fingerprint),
            modifier = Modifier.size(120.dp),
            tint = Color.Unspecified
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.no_tasks_selected_yet),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(Res.string.please_click_plus_button_to_add_one),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
