package org.openeel.app.view.enrollment.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.respectPagingItems
import org.openeel.app.components.respectRememberPager
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.EnrollmentDataSource
import org.openeel.datalayer.school.model.Enrollment
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.delete
import org.openeel.shared.generated.resources.edit
import org.openeel.shared.generated.resources.enrollment_for
import org.openeel.shared.generated.resources.more_options
import org.openeel.shared.util.ext.label
import org.openeel.shared.viewmodel.enrollment.list.EnrollmentListUiState
import org.openeel.shared.viewmodel.enrollment.list.EnrollmentListViewModel
import org.openeel.shared.util.rememberFormattedDate


@Composable
fun EnrollmentListScreen(
    viewModel: EnrollmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    EnrollmentListScreen(
        uiState = uiState,
        onClickEdit = viewModel::onEditEnrollment,
        onClickDelete = viewModel::onDeleteEnrollment
    )
}

@Composable
fun EnrollmentListScreen(
    uiState: EnrollmentListUiState,
    onClickEdit: (Enrollment?) -> Unit,
    onClickDelete: (Enrollment) -> Unit,
) {
    val pager = respectRememberPager(uiState.enrollments)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    //Ensure that there is not more than one expanded item at a time
    var expandedItemUid by remember { mutableStateOf<String?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item("header") {
            val personName = uiState.forPerson.dataOrNull()?.fullName() ?: ""
            Text(
                text = stringResource(Res.string.enrollment_for) + personName,
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            )
        }

        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.uid ?: index.toString() },
            contentType = { EnrollmentDataSource.ENDPOINT_NAME },
        ) { enrollment ->
            val beginDate = enrollment?.beginDate?.let { rememberFormattedDate(it) } ?: ""
            val endDate = enrollment?.endDate?.let { rememberFormattedDate(it) } ?: ""

            val isExpanded = expandedItemUid == enrollment?.uid
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = {
                    Text("$beginDate - $endDate")
                },
                supportingContent = {
                    Text(
                        text = enrollment?.role?.label?.let { stringResource(it) } ?: "",
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = {
                            expandedItemUid = if (isExpanded) null else enrollment?.uid
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(resource = Res.string.more_options)
                        )
                    }

                    DropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { expandedItemUid = null }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.edit)) },
                            onClick = {
                                expandedItemUid = null
                                onClickEdit(enrollment)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.delete)) },
                            onClick = {
                                expandedItemUid = null
                                enrollment?.also(onClickDelete)
                            }
                        )
                    }
                }
            )
        }
    }
}
