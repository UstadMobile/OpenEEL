package world.respect.app.view.clazz.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.model.Clazz
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.class_name
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.required
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.clazz.edit.ClazzEditUiState
import world.respect.shared.viewmodel.clazz.edit.ClazzEditViewModel

@Composable
fun ClazzEditScreen(
    viewModel: ClazzEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    ClazzEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
        onClearError =  viewModel::onClearError
    )
}

@Composable
fun ClazzEditScreen(
    uiState: ClazzEditUiState,
    onEntityChanged: (Clazz) -> Unit = {},
    onClearError: () -> Unit = {},
) {

    val clazz = uiState.clazz.dataOrNull()
    val fieldsEnabled = uiState.fieldsEnabled

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("name"),
            value = clazz?.title ?: "",
            label = {
                Text(stringResource(Res.string.class_name) + "*")
            },
            onValueChange = { value ->
                clazz?.also {
                    onEntityChanged(it.copy(title = value))
                }
                if (uiState.clazzNameError != null && value.isNotBlank()) {
                    onClearError()
                }
            },
            singleLine = true,
            supportingText = {
                Text(uiTextStringResource(uiState.clazzNameError ?: Res.string.required.asUiText()))
            },
            enabled = fieldsEnabled,
            isError = uiState.clazzNameError != null
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag(("description")),
            value = clazz?.description ?: "",
            label = {
                Text(
                    stringResource(Res.string.description)
                )
            },
            onValueChange = { newValue ->
                clazz?.also {
                    onEntityChanged(it.copy(description = newValue))
                }
            }
        )
    }
}

