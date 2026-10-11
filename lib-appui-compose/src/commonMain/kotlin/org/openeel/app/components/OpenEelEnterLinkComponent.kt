package com.ustadmobile.libuicompose.org.openeel.app.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.example_url_placeholder
import org.openeel.shared.generated.resources.link_label
import org.openeel.shared.generated.resources.next
import org.openeel.shared.resources.UiText

@Composable
fun OpenEelEnterLinkColumn(
    linkUrl: String,
    errorMessage: UiText?,
    onLinkChanged: (String) -> Unit,
    onClickNext: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonContent: @Composable RowScope.() -> Unit = {
        Text(stringResource(Res.string.next))
    }
) {
    Column(
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = linkUrl,
            onValueChange = onLinkChanged,
            label = {
                Text(
                    text = stringResource(Res.string.link_label)
                )
            },
            placeholder = {
                Text(
                    text = stringResource(Res.string.example_url_placeholder),
                )
            },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null,
            supportingText = errorMessage?.let {
                {
                    Text(
                        text = uiTextStringResource(it)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            content = buttonContent,
        )
    }
}