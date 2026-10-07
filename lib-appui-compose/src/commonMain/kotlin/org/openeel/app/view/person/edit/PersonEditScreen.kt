package org.openeel.app.view.person.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.RespectExposedDropDownMenuField
import org.openeel.app.components.OpenEelGenderExposedDropDownMenuField
import org.openeel.app.components.OpenEelLocalDateField
import org.openeel.app.components.OpenEelPersonAvatar
import org.openeel.app.components.OpenEelPhoneNumberTextField
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.date_of_birth
import org.openeel.shared.generated.resources.email
import org.openeel.shared.generated.resources.family_member
import org.openeel.shared.generated.resources.family_members
import org.openeel.shared.generated.resources.first_names
import org.openeel.shared.generated.resources.last_name
import org.openeel.shared.generated.resources.phone_number
import org.openeel.shared.generated.resources.remove
import org.openeel.shared.generated.resources.required
import org.openeel.shared.generated.resources.role
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.label
import org.openeel.shared.viewmodel.person.edit.PersonEditUiState
import org.openeel.shared.viewmodel.person.edit.PersonEditViewModel

@Composable
fun PersonEditScreen(
    viewModel: PersonEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    PersonEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
        onNationalNumberSetChanged = viewModel::onNationalPhoneNumSetChanged,
        onClickAddFamilyMember = viewModel::onClickAddFamilyMember,
        onRemoveFamilyMember = viewModel::onRemoveFamilyMember,
    )
}

@Composable
fun PersonEditScreen(
    uiState: PersonEditUiState,
    onEntityChanged: (Person) -> Unit,
    onNationalNumberSetChanged: (Boolean) -> Unit,
    onClickAddFamilyMember: () -> Unit,
    onRemoveFamilyMember: (Person) -> Unit,
) {
    val person = uiState.person
    val fieldsEnabled = uiState.fieldsEnabled
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        OutlinedTextField(
            modifier = Modifier.testTag("first_names")
                .fillMaxWidth().defaultItemPadding(top = 16.dp),
            value = person?.givenName ?: "",
            label = { Text(stringResource(Res.string.first_names) + "*") },
            onValueChange = { value ->
                person?.also {
                    onEntityChanged(it.copy(givenName = value))
                }
            },
            isError = uiState.firstNameError != null,
            singleLine = true,
            enabled = fieldsEnabled,
            supportingText = {
                Text(uiTextStringResource(uiState.firstNameError ?: Res.string.required.asUiText()))
            }
        )

        OutlinedTextField(
            modifier = Modifier.testTag("last_name").fillMaxWidth().defaultItemPadding(),
            value = person?.familyName ?: "",
            label = { Text(stringResource(Res.string.last_name) + "*") },
            onValueChange = { value ->
                person?.also {
                    onEntityChanged(it.copy(familyName = value))
                }
            },
            isError = uiState.lastNameError != null,
            singleLine = true,
            supportingText = {
                Text(uiTextStringResource(uiState.lastNameError ?: Res.string.required.asUiText()))
            }
        )

        OpenEelGenderExposedDropDownMenuField(
            value = person?.gender ?: PersonGenderEnum.UNSPECIFIED,
            onValueChanged = { gender ->
                person?.also {
                    onEntityChanged(it.copy(gender = gender))
                }
            },
            modifier = Modifier.testTag("gender").fillMaxWidth().defaultItemPadding(),
            isError = uiState.genderError != null,
            errorText = uiState.genderError
        )

        if(uiState.showRoleDropdown) {
            val roleEnumVal = person?.roles?.first()?.roleEnum ?: PersonRoleEnum.STUDENT
            RespectExposedDropDownMenuField(
                value = roleEnumVal,
                modifier = Modifier.defaultItemPadding().fillMaxWidth().testTag("role"),
                label = {
                    Text(stringResource(Res.string.role) + "*")
                },
                onOptionSelected = { newRole ->
                    person?.also {
                        onEntityChanged(
                            it.copy(
                                roles = listOf(
                                    PersonRole(
                                        isPrimaryRole = true,
                                        roleEnum = newRole,
                                    )
                                )
                            )
                        )
                    }
                },
                options = uiState.roleOptions,
                itemText = { stringResource(it.label) },
                enabled = uiState.fieldsEnabled,
                supportingText = {
                    Text(stringResource(Res.string.required))
                }
            )
        }

        if (uiState.showFamilyMembers) {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = stringResource(Res.string.family_members),
                style = MaterialTheme.typography.bodySmall,
            )
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddFamilyMember()
                },
                headlineContent = {
                    Text(stringResource(Res.string.family_member))
                },
                leadingContent = {
                    Icon(Icons.Default.Add, contentDescription = "")
                }
            )
            val familyMembers = uiState.familyMembers
            familyMembers.forEach { familyPerson ->
                ListItem(
                    leadingContent = {
                        OpenEelPersonAvatar(familyPerson.fullName())
                    },
                    headlineContent = {
                        Text(familyPerson.fullName())
                    },
                    trailingContent = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(Res.string.remove),
                            modifier = Modifier.clickable {
                                onRemoveFamilyMember(familyPerson)
                            }
                        )
                    }
                )
            }
        }

        OpenEelLocalDateField(
            modifier = Modifier.testTag("date_of_birth").fillMaxWidth().defaultItemPadding(),
            value = person?.dateOfBirth,
            label = { Text(stringResource(Res.string.date_of_birth)) },
            onValueChange = { date ->
                person?.also {
                    onEntityChanged(it.copy(dateOfBirth = date))
                }
            },
            isError = uiState.dateOfBirthError != null,
            enabled = uiState.fieldsEnabled,
            supportingText = uiState.dateOfBirthError?.let {
                { Text(uiTextStringResource(it)) }
            }
        )

        OpenEelPhoneNumberTextField(
            value = person?.phoneNumber ?: "",
            modifier = Modifier.testTag("phone_number").fillMaxWidth().defaultItemPadding(),
            label = { Text(stringResource(Res.string.phone_number)) },
            onValueChange = { phoneNumber ->
                person?.also {
                    onEntityChanged(it.copy(phoneNumber = phoneNumber))
                }
            },
            onNationalNumberSetChanged = onNationalNumberSetChanged,
            isError = uiState.phoneNumError != null,
            supportingText = uiState.phoneNumError?.let {
                { Text(uiTextStringResource(it)) }
            },
            countryCodeTestTag = "phone_countrycode",
            numberTextFieldTestTag = "phone_number"
        )

        OutlinedTextField(
            modifier = Modifier.testTag("email").fillMaxWidth().defaultItemPadding(),
            value = person?.email ?: "",
            label = { Text(stringResource(Res.string.email)) },
            singleLine = true,
            onValueChange = { email ->
                person?.also {
                    onEntityChanged(it.copy(email = email))
                }
            },
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let {
                { Text(uiTextStringResource(it)) }
            },
            enabled = uiState.fieldsEnabled
        )
    }

}
