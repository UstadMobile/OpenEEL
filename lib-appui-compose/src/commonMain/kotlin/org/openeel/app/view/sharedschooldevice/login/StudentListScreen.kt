package org.openeel.app.view.sharedschooldevice.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import org.openeel.app.components.RespectPersonAvatar
import org.openeel.app.components.respectPagingItems
import org.openeel.app.components.respectRememberPager
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.viewmodel.sharedschooldevice.login.StudentListUiState
import org.openeel.shared.viewmodel.sharedschooldevice.login.StudentListViewModel

@Composable
fun StudentListScreen(
    viewModel: StudentListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    StudentListScreen(
        uiState = uiState,
        onClickStudent = viewModel::onClickStudent,
    )
}

@Composable
fun StudentListScreen(
    uiState: StudentListUiState,
    onClickStudent: (Person) -> Unit,
) {
    val pager = respectRememberPager(uiState.students)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.guid ?: index.toString() },
            contentType = { ClassDataSource.ENDPOINT_NAME },
        ) { student ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        student?.also(onClickStudent)
                    },

                leadingContent = {
                    RespectPersonAvatar(name = student?.fullName() ?: "")
                },

                headlineContent = {
                    Text(text = student?.fullName() ?: "")
                }
            )
        }
    }
}