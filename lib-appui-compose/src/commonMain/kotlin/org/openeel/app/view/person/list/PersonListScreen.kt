package world.respect.app.view.person.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ext.primaryRole
import world.respect.datalayer.school.ext.primaryRoleOrNull
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.accept_invite
import world.respect.shared.generated.resources.add_person
import world.respect.shared.generated.resources.dismiss_invite
import world.respect.shared.generated.resources.gender_literal
import world.respect.shared.generated.resources.invite_person
import world.respect.shared.generated.resources.pending_requests_to_join
import world.respect.shared.util.ext.fullName
import world.respect.shared.util.ext.label
import world.respect.shared.viewmodel.person.list.PersonListUiState
import world.respect.shared.viewmodel.person.list.PersonListViewModel

@Composable
fun PersonListScreen(
    viewModel: PersonListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    PersonListScreen(
        uiState = uiState,
        onClickItem = viewModel::onClickItem,
        onClickAddPerson = viewModel::onClickAdd,
        onClickInvitePerson = viewModel::onClickInvitePerson,
        onClickAcceptOrDismissInvite = viewModel::onClickAcceptOrDismissInvite,
        onTogglePendingInvites = viewModel::onTogglePendingInvites,
    )
}

@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    onClickItem: (PersonListDetails) -> Unit,
    onClickAddPerson: () -> Unit,
    onClickAcceptOrDismissInvite: (Person, Boolean) -> Unit,
    onClickInvitePerson: () -> Unit,
    onTogglePendingInvites: () -> Unit,
) {
    val pager = respectRememberPager(uiState.persons)
    val pendingPager = respectRememberPager(uiState.pendingPersons)
    val pendingItems = pendingPager.flow.collectAsLazyPagingItems()


    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if(uiState.showAddPersonItem) {
            item("add_person") {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickAddPerson()
                    },
                    headlineContent = {
                        Text(stringResource(Res.string.add_person))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Add,
                            modifier = Modifier.size(40.dp).padding(8.dp),
                            contentDescription = null)
                    }
                )
            }
        }

        if(uiState.showInvite) {
            item("invite_person") {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickInvitePerson()
                    },
                    headlineContent = {
                        Text(stringResource(Res.string.invite_person))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Share,
                            modifier = Modifier.size(40.dp).padding(8.dp),
                            contentDescription = null)
                    }
                )
            }
        }

        if (pendingItems.itemCount > 0) {
            item("pending_person_header") {
                ListItem(
                    modifier = Modifier.clickable { onTogglePendingInvites() },
                    headlineContent = {
                        Text(stringResource(Res.string.pending_requests_to_join)+
                                "(${pendingItems.itemCount})")
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(if (uiState.isPendingExpanded) 0f else -90f),
                            contentDescription = null
                        )
                    }
                )
            }
        }

        if (uiState.isPendingExpanded) {
            respectPagingItems(
                items = pendingItems,
                key = { item, index -> (item?.guid + index.toString()) }
            ) { person ->
                val showApproveOption = person?.roles?.firstOrNull()?.roleEnum?.let {
                    uiState.showApproveOption(it)
                } ?: false

                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    leadingContent = {
                        RespectPersonAvatar(
                            name = person?.fullName() ?: ""
                        )
                    },
                    headlineContent = {
                        Text(
                            text = person?.fullName().orEmpty()
                        )
                    },
                    supportingContent = {
                        person?.primaryRoleOrNull()?.also { primaryRole ->
                            Text(stringResource(primaryRole.label))
                        }
                    },
                    trailingContent = if(showApproveOption) {
                        {
                            Row {
                                Icon(
                                    modifier = Modifier.size(24.dp)
                                        .clickable {
                                            onClickAcceptOrDismissInvite(person, true)
                                        },
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = stringResource(resource = Res.string.accept_invite)
                                )

                                Spacer(Modifier.width(16.dp))

                                Icon(
                                    modifier = Modifier.size(24.dp).clickable {
                                        onClickAcceptOrDismissInvite(person, false)
                                    },
                                    imageVector = Icons.Outlined.Cancel,
                                    contentDescription = stringResource(resource = Res.string.dismiss_invite)
                                )
                            }
                        }
                    }else {
                        null
                    }
                )
            }
        }

        item(key = "horizontaldiv") {
            HorizontalDivider(Modifier.height(1.dp))
        }

        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.guid ?: index.toString() },
            contentType = { PersonDataSource.ENDPOINT_NAME },
        ) { person ->
            PersonListItem(
                person = person,
                modifier = Modifier.clickable {
                    person?.also(onClickItem)
                }
            )
        }
    }
}
