package world.respect.app.view.manageuser.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectGenderExposedDropDownMenuField
import world.respect.app.components.RespectLocalDateField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.required
import world.respect.shared.viewmodel.manageuser.profile.SignupUiState
import world.respect.shared.viewmodel.manageuser.profile.SignupViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    SignupScreen(
        uiState = uiState,
        onFullNameChanged = viewModel::onFullNameChanged,
        onGenderChanged = viewModel::onGenderChanged,
        onDateOfBirthChanged = viewModel::onDateOfBirthChanged,
        onPersonPictureUriChanged = viewModel::onPersonPictureChanged
    )
}

@Composable
fun SignupScreen(
    uiState: SignupUiState,
    onFullNameChanged: (String) -> Unit,
    onGenderChanged: (PersonGenderEnum) -> Unit,
    onDateOfBirthChanged: (LocalDate?) -> Unit,
    onPersonPictureUriChanged: (String?) -> Unit = { },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.personInfo.name,
            onValueChange = onFullNameChanged,
            label = { uiState.nameLabel?.let { Text(uiTextStringResource(it) + "*") } },
            isError = uiState.fullNameError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            supportingText = {
                Text(uiState.fullNameError?.let { uiTextStringResource(it) }
                    ?: stringResource(Res.string.required)
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        RespectGenderExposedDropDownMenuField(
            value = uiState.personInfo.gender,
            onValueChanged = onGenderChanged,
            modifier = Modifier.testTag("gender").fillMaxWidth(),
            isError = uiState.genderError != null,
            errorText = uiState.genderError
        )

        Spacer(Modifier.height(16.dp))

        RespectLocalDateField(
            modifier = Modifier.fillMaxWidth().testTag("dateOfBirth"),
            value = uiState.personInfo.dateOfBirth.takeIf {
                it != RespectRedeemInviteRequest.DATE_OF_BIRTH_EPOCH
            },
            onValueChange = {onDateOfBirthChanged(it) },
            isError = uiState.dateOfBirthError!=null,
            label = {
                uiState.dateOfBirthLabel?.let {  Text(uiTextStringResource(it) + "*") }
            },
            supportingText = {
                Text(uiState.dateOfBirthError?.let { uiTextStringResource(it) }
                    ?: stringResource(Res.string.required))
            }
        )
    }

}
