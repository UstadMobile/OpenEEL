package org.openeel.app.view.assignment.edit

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
import org.openeel.app.components.LangMapTextField
import org.openeel.app.components.OpenEelLocalDateTimeField
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.datalayer.school.model.Clazz
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.ext.copyWithObjectActivityDescription
import org.openeel.lib.xapi.ext.copyWithObjectActivityName
import org.openeel.lib.xapi.ext.extensionDeadlineAsInstantOrNull
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.shared.domain.xapi.withDeadline
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.assign_to
import org.openeel.shared.generated.resources.assignment_title
import org.openeel.shared.generated.resources.description
import org.openeel.shared.generated.resources.task
import org.openeel.shared.generated.resources.due_date
import org.openeel.shared.generated.resources.fingerprint
import org.openeel.shared.generated.resources.no_tasks_selected_yet
import org.openeel.shared.generated.resources.please_click_plus_button_to_add_one
import org.openeel.shared.generated.resources.required
import org.openeel.shared.generated.resources.tasks
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.assignment.edit.AssignmentEditUiState
import org.openeel.shared.viewmodel.assignment.edit.AssignmentEditViewModel
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

        OpenEelLocalDateTimeField(
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
