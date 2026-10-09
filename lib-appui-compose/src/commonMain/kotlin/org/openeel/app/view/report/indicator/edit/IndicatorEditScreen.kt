package world.respect.app.view.report.indicator.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.extensions.reportoptions.Indicator
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.field
import world.respect.shared.generated.resources.sql
import world.respect.shared.viewmodel.report.indictor.edit.IndicatorEditUiState
import world.respect.shared.viewmodel.report.indictor.edit.IndicatorEditViewModel

@Composable
fun IndictorEditScreen(
    viewModel: IndicatorEditViewModel
) {
    val uiState: IndicatorEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = IndicatorEditUiState(),
        context = Dispatchers.Main.immediate
    )
    IndictorEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged
    )

}

@Composable
fun IndictorEditScreen(
    uiState: IndicatorEditUiState,
    onEntityChanged: (Indicator) -> Unit,
) {
    val indicator = uiState.indicatorData.dataOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = indicator?.name ?: "",
                label = { Text(stringResource(Res.string.field) + "*") },
                singleLine = true,
                onValueChange = { newName ->
                    indicator?.also {
                        onEntityChanged(it.copy(name = newName))
                    }
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = indicator?.description ?: "",
                label = { Text(stringResource(Res.string.description) + "*") },
                singleLine = true,
                onValueChange = { newDesc ->
                    indicator?.also {
                        onEntityChanged(it.copy(description = newDesc))
                    }
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp),
                value = indicator?.sql ?: "",
                label = { Text(stringResource(Res.string.sql) + "*") },
                onValueChange = { newSql ->
                    indicator?.also {
                        onEntityChanged(it.copy(sql = newSql))
                    }
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}