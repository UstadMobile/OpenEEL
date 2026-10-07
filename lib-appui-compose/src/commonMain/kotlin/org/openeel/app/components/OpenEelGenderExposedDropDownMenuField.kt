package org.openeel.app.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.gender
import org.openeel.shared.generated.resources.required
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.label

@Composable
fun OpenEelGenderExposedDropDownMenuField(
    value: PersonGenderEnum,
    onValueChanged: (PersonGenderEnum) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    errorText: UiText?,
) {
    RespectExposedDropDownMenuField(
        value = value,
        options = PersonGenderEnum.entries.filter {
            it != PersonGenderEnum.UNSPECIFIED
        },
        onOptionSelected = onValueChanged,
        modifier = modifier,
        itemText = {
            if(it == PersonGenderEnum.UNSPECIFIED) {
                ""
            } else {
                stringResource(it.label)
            }
        },
        label = {
            Text(stringResource(Res.string.gender) + "*")
        },
        supportingText = {
            Text(uiTextStringResource(errorText ?: Res.string.required.asUiText()))
        },
        isError = isError,
        enabled = enabled,
        logline = "Gender dropdown",
    )
}