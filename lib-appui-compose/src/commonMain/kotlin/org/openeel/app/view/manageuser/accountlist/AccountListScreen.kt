package org.openeel.app.view.manageuser.accountlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelLongVersionInfoItem
import org.openeel.app.components.OpenEelPersonAvatar
import org.openeel.app.components.defaultItemPadding
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.domain.account.UserAccount
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_account
import org.openeel.shared.generated.resources.app_name
import org.openeel.shared.generated.resources.developed_by
import org.openeel.shared.generated.resources.family_members
import org.openeel.shared.generated.resources.license_text
import org.openeel.shared.generated.resources.logout
import org.openeel.shared.generated.resources.profile
import org.openeel.shared.generated.resources.app_is_open_source
import org.openeel.shared.generated.resources.supported_by_spix_foundation
import org.openeel.shared.generated.resources.send_feedback
import org.openeel.shared.viewmodel.manageuser.accountlist.AccountListUiState
import org.openeel.shared.viewmodel.manageuser.accountlist.AccountListViewModel

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    AccountListScreen(
        uiState = uiState,
        onClickAccount = viewModel::onClickAccount,
        onClickAddAccount = viewModel::onClickAddAccount,
        onClickLogout = viewModel::onClickLogout,
        onClickFamilyPerson = viewModel::onClickFamilyPerson,
        onClickProfile = viewModel::onClickProfile,
        onClickShareFeedback = viewModel::onClickShareFeedback
    )
}

@Composable
fun AccountListScreen(
    uiState: AccountListUiState,
    onClickAccount: (UserAccount) -> Unit,
    onClickFamilyPerson: (Person) -> Unit,
    onClickAddAccount: () -> Unit,
    onClickLogout: () -> Unit,
    onClickProfile: () -> Unit,
    onClickShareFeedback: () -> Unit
) {
    val familyPersons = uiState.selectedAccount?.relatedPersons ?: emptyList()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        uiState.selectedAccount?.also { activeAccount ->
            item("selected_account") {
                AccountListItem(
                    account = activeAccount,
                    onClickAccount = null,
                    extras = {
                        Row {
                            if(uiState.showSelectedAccountProfileButton) {
                                OutlinedButton(
                                    onClick = onClickProfile,
                                ) {
                                    Text(stringResource(Res.string.profile))
                                }

                                Spacer(Modifier.width(16.dp))
                            }

                            OutlinedButton(onClick = onClickLogout) {
                                Text(stringResource(Res.string.logout))
                            }
                        }
                    }
                )
            }
        }

        if (!familyPersons.isEmpty()) {
            item("family_member_header") {
                Text(
                    modifier = Modifier.defaultItemPadding(),
                    text = stringResource(Res.string.family_members)
                )
            }

            items(
                items = familyPersons,
                key = { it.guid }
            ) { account ->
                ListItem(
                    modifier = Modifier.clickable(
                        enabled = uiState.familyMembersClickEnabled,
                    ) {
                        onClickFamilyPerson(account)
                    },
                    leadingContent = {
                        OpenEelPersonAvatar(name = account.fullName())
                    },
                    headlineContent = {
                        Text(account.fullName())
                    }
                )
            }
        }

        item("divider1") {
            HorizontalDivider()
        }

        items(
            items = uiState.accounts,
            key = { it.session.account.userGuid }
        ) { account ->
            AccountListItem(
                account = account,
                onClickAccount = onClickAccount,
            )
        }

        item("divider2") {
            HorizontalDivider()
        }

        item("add_account") {
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddAccount()
                },
                headlineContent = {
                    Text(stringResource(Res.string.add_account))
                },
                leadingContent = {
                    Icon(Icons.Default.Add, contentDescription = "")
                }
            )
        }
        item("divider3") {
            HorizontalDivider()
        }
        item {
            ListItem(
                modifier = Modifier.clickable {
                    onClickShareFeedback()
                },
                headlineContent = {
                    Text(
                        stringResource(
                            Res.string.send_feedback
                        )
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = stringResource(Res.string.send_feedback)
                    )
                }
            )
        }

        item("version_info") {
            OpenEelLongVersionInfoItem()
        }

        item("copyright_info") {
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text(stringResource(Res.string.developed_by))
                },
                supportingContent = {
                    Column {
                        Text(stringResource(Res.string.supported_by_spix_foundation))
                        Text(stringResource(Res.string.app_is_open_source, Res.string.app_name))
                        Text(
                            stringResource(Res.string.license_text, stringResource(Res.string.app_name))
                        )
                    }
                }
            )
        }
    }
}
