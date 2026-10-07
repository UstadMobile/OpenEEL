package org.openeel.app.view.person.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelDetailField
import org.openeel.app.components.OpenEelPersonAvatar
import org.openeel.app.components.OpenEelQuickActionButton
import org.openeel.app.components.defaultItemPadding
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.create_account
import org.openeel.shared.generated.resources.date_of_birth
import org.openeel.shared.generated.resources.email
import org.openeel.shared.generated.resources.family_members
import org.openeel.shared.generated.resources.gender
import org.openeel.shared.generated.resources.manage_account
import org.openeel.shared.generated.resources.phone_number
import org.openeel.shared.generated.resources.role
import org.openeel.shared.generated.resources.username_label
import org.openeel.shared.util.ext.label
import org.openeel.shared.viewmodel.person.detail.PersonDetailUiState
import org.openeel.shared.viewmodel.person.detail.PersonDetailViewModel

@Composable
fun PersonDetailScreen(
    viewModel: PersonDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    PersonDetailScreen(
        uiState = uiState,
        onClickManageAccount = viewModel::navigateToManageAccount,
        onClickCreateAccount = viewModel::onClickCreateAccount,
        onClickPhoneNumber = viewModel::onClickPhoneNumber,
        onClickFamilyMember = viewModel::onClickFamilyMember
    )
}

@Composable
fun PersonDetailScreen(
    uiState: PersonDetailUiState,
    onClickManageAccount:() -> Unit,
    onClickCreateAccount: () -> Unit,
    onClickPhoneNumber: () -> Unit,
    onClickFamilyMember: (String) -> Unit,
) {
    val person = uiState.person

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (uiState.manageAccountVisible){
                OpenEelQuickActionButton(
                    labelText = stringResource(Res.string.manage_account),
                    imageVector = Icons.Default.Key,
                    onClick = onClickManageAccount
                )
            }

            if(uiState.createAccountVisible){
                OpenEelQuickActionButton(
                    labelText = stringResource(Res.string.create_account),
                    imageVector = Icons.Default.Key,
                    onClick = onClickCreateAccount,
                )
            }
        }

        HorizontalDivider()

        person?.roles?.firstOrNull()?.also { role ->
            OpenEelDetailField(
                modifier = Modifier.defaultItemPadding(),
                value = { Text(stringResource(role.roleEnum.label)) },
                label = { Text(stringResource(Res.string.role)) },
            )
        }

        person?.username?.also {
            OpenEelDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { Text(stringResource(Res.string.username_label)) },
                value = { Text(it) }
            )
        }

        OpenEelDetailField(
            modifier = Modifier.defaultItemPadding(),
            label = { Text(stringResource(Res.string.gender)) },
            value = { Text(person?.gender?.label?.let { stringResource(it) } ?: "")}
        )

        person?.dateOfBirth?.also {
            OpenEelDetailField(
                modifier = Modifier.defaultItemPadding(),
                label = { (Text(stringResource(Res.string.date_of_birth))) },
                value = { Text(it.toString()) }
            )
        }
        person?.phoneNumber?.also {
            OpenEelDetailField(
                modifier = Modifier.defaultItemPadding().fillMaxWidth().clickable {
                    onClickPhoneNumber()
                },
                label = { Text(stringResource(Res.string.phone_number)) },
                value = { Text(it) }
            )
        }
        person?.email
            ?.takeIf { it.isNotBlank() }
            ?.also { email ->
                OpenEelDetailField(
                    modifier = Modifier.defaultItemPadding(),
                    label = { Text(stringResource(Res.string.email)) },
                    value = { Text(email) }
                )
            }

        if (uiState.familyMembers.isNotEmpty()) {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = stringResource(Res.string.family_members),
                style = MaterialTheme.typography.bodySmall,
            )

            uiState.familyMembers.forEach { familyPerson->
                ListItem(
                    modifier = Modifier.clickable {
                        onClickFamilyMember(familyPerson.guid)
                    },
                    leadingContent = {
                        OpenEelPersonAvatar(familyPerson.fullName())
                    },
                    headlineContent = {
                        Text(familyPerson.fullName())
                    }
                )
            }
        }
    }
}
