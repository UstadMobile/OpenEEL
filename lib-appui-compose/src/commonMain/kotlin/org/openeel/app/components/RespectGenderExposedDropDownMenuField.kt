package world.respect.app.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.gender
import world.respect.shared.generated.resources.required
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.generated.resources.required_field
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.label

@Composable
fun RespectGenderExposedDropDownMenuField(
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