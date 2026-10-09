package world.respect.app.view.manageuser.accountlist

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
import world.respect.app.components.RespectLongVersionInfoItem
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.defaultItemPadding
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.Person
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_account
import world.respect.shared.generated.resources.developed_by
import world.respect.shared.generated.resources.family_members
import world.respect.shared.generated.resources.license_text
import world.respect.shared.generated.resources.logout
import world.respect.shared.generated.resources.profile
import world.respect.shared.generated.resources.respect_is_open_source
import world.respect.shared.generated.resources.supported_by_spix_foundation
import world.respect.shared.generated.resources.send_feedback
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListUiState
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListViewModel

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
    onClickAccount: (RespectAccount) -> Unit,
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
                        RespectPersonAvatar(name = account.fullName())
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
            if (account.session.profilePersonUid != null) {
                AccountListItem(
                    account = account,
                    onClickAccount = onClickAccount,
                )
            }
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
            RespectLongVersionInfoItem()
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
                        Text(stringResource(Res.string.respect_is_open_source))
                        Text(stringResource(Res.string.license_text))
                    }
                }
            )
        }
    }
}
