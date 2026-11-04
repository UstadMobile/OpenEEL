package org.openeel.app.view.clazz.edit

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
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.model.Clazz
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.class_name
import org.openeel.shared.generated.resources.description
import org.openeel.shared.generated.resources.required
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.clazz.edit.ClazzEditUiState
import org.openeel.shared.viewmodel.clazz.edit.ClazzEditViewModel

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

