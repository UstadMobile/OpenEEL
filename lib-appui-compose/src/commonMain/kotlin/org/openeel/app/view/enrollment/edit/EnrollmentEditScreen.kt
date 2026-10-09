package world.respect.app.view.enrollment.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectLocalDateField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.model.Enrollment
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.end_date_label
import world.respect.shared.generated.resources.start_date_label
import world.respect.shared.viewmodel.enrollment.edit.EnrollmentEditUiState
import world.respect.shared.viewmodel.enrollment.edit.EnrollmentEditViewModel

@Composable
fun EnrollmentEditScreen(
    viewModel: EnrollmentEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    EnrollmentEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged
    )
}

@Composable
fun EnrollmentEditScreen(
    uiState: EnrollmentEditUiState,
    onEntityChanged: (Enrollment) -> Unit
) {

    val enrollment = uiState.enrollment.dataOrNull()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        RespectLocalDateField(
            modifier = Modifier.testTag("begin_date").fillMaxWidth().defaultItemPadding(),
            value = enrollment?.beginDate,
            label = { Text(stringResource(Res.string.start_date_label)) },
            onValueChange = { date ->
                enrollment?.also {
                    onEntityChanged(it.copy(beginDate = date))
                }
            },
            isError = uiState.beginDateError != null,
            enabled = uiState.fieldsEnabled,
            supportingText = uiState.beginDateError?.let {
                { Text(uiTextStringResource(it)) }
            }
        )

        RespectLocalDateField(
            modifier = Modifier
                .testTag("end_date")
                .fillMaxWidth()
                .defaultItemPadding(),
            value = enrollment?.endDate,
            label = { Text(stringResource(Res.string.end_date_label)) },
            onValueChange = { date ->
                enrollment?.also {
                    onEntityChanged(it.copy(endDate = date))
                }
            },
            enabled = uiState.fieldsEnabled,
        )
    }
}